package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.SendOrderConfirmationUseCase;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationRenderer;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationSender;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        properties = {
            "spring.kafka.listener.auto-startup=false",
            "spring.kafka.consumer.auto-offset-reset=earliest",
            "notifications.email.kafka.group=confirmation-recovery-it",
            "ordering.events.kafka.enabled=false"
        })
class OrderConfirmationKafkaIT extends AbstractIntegrationTest {
    private static final String GROUP = "confirmation-recovery-it";
    private static final String TOPIC = OrderPlacedIntegrationEvent.EVENT_TYPE;
    @Autowired OrderConfirmationDelivery deliveries;
    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired KafkaListenerEndpointRegistry registry;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    @MockitoSpyBean SendOrderConfirmationUseCase confirmations;
    @MockitoBean OrderConfirmationRenderer renderer;
    @MockitoBean OrderConfirmationSender sender;

    @AfterEach
    void stopListener() {
        registry.stop();
    }

    @Test
    void futureRetryKeepsOffsetUncommittedUntilDeliveryBecomesDue() throws Exception {
        assertPendingDelivery("FAILED", false);
    }

    @Test
    void restartingDuringLiveLeaseRedeliversAfterExpiry() throws Exception {
        assertPendingDelivery("CLAIMED", true);
    }

    private void assertPendingDelivery(String status, boolean restart) throws Exception {
        var event = event();
        Instant now = Instant.now();
        Instant eligibleAt = now.plusSeconds(15);
        UUID token = UUID.randomUUID();
        deliveries.claim(event.eventId(), event.orderNumber(), token, now, eligibleAt);
        if (status.equals("FAILED")) {
            deliveries.markFailed(event.eventId(), token, "mail unavailable", eligibleAt, false);
        }
        var sentAt = new AtomicReference<Instant>();
        when(renderer.render(event))
                .thenReturn(
                        new RenderedOrderConfirmation(
                                "shop@example.com",
                                "ada@example.com",
                                "Order placed",
                                "text",
                                "html"));
        doAnswer(
                        invocation -> {
                            sentAt.set(Instant.now());
                            return null;
                        })
                .when(sender)
                .send(any());
        long offset = publish(event);
        try (Admin admin = admin()) {
            registry.start();
            verify(confirmations, timeout(10000)).send(event);
            await().during(Duration.ofSeconds(2))
                    .atMost(Duration.ofSeconds(3))
                    .untilAsserted(
                            () -> {
                                assertThat(committedOffset(admin)).isLessThanOrEqualTo(offset);
                                assertThat(sentAt.get()).isNull();
                                verify(confirmations).send(event);
                            });
            if (restart) {
                registry.stop();
                assertThat(committedOffset(admin)).isLessThanOrEqualTo(offset);
                registry.start();
                verify(confirmations, timeout(10000).atLeast(2)).send(event);
            }
            await().atMost(Duration.ofSeconds(30))
                    .untilAsserted(
                            () -> {
                                assertThat(sentAt.get()).isAfterOrEqualTo(eligibleAt);
                                assertThat(committedOffset(admin)).isGreaterThan(offset);
                                assertThat(
                                                jdbc.queryForObject(
                                                        "select status from order_confirmation_delivery where event_id = ?",
                                                        String.class,
                                                        event.eventId()))
                                        .isEqualTo("SENT");
                            });
            verify(sender).send(any());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENT", "QUARANTINED"})
    void terminalDeliveryCommitsWithoutSendingOrRetrying(String status) throws Exception {
        var event = event();
        Instant now = Instant.now();
        UUID token = UUID.randomUUID();
        deliveries.claim(event.eventId(), event.orderNumber(), token, now, now.plusSeconds(300));
        if (status.equals("SENT")) {
            deliveries.markSent(event.eventId(), token, now);
        } else {
            deliveries.markFailed(event.eventId(), token, "permanent", now, true);
        }
        long offset = publish(event);
        try (Admin admin = admin()) {
            registry.start();
            await().atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> assertThat(committedOffset(admin)).isGreaterThan(offset));
            verify(confirmations).send(event);
            verifyNoInteractions(renderer, sender);
        }
    }

    private long publish(OrderPlacedIntegrationEvent event) throws Exception {
        return kafka.send(TOPIC, 0, event.eventId().toString(), mapper.writeValueAsString(event))
                .get(10, TimeUnit.SECONDS)
                .getRecordMetadata()
                .offset();
    }

    private Admin admin() {
        return Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    private long committedOffset(Admin admin) throws Exception {
        var offsets =
                admin.listConsumerGroupOffsets(GROUP)
                        .partitionsToOffsetAndMetadata()
                        .get(5, TimeUnit.SECONDS);
        var offset = offsets.get(new TopicPartition(TOPIC, 0));
        return offset == null ? -1 : offset.offset();
    }

    private OrderPlacedIntegrationEvent event() {
        var address =
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null);
        return new OrderPlacedIntegrationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-2026-100001",
                42L,
                "Ada",
                "ada@example.com",
                Instant.now(),
                new BigDecimal("39.98"),
                Currency.EUR,
                List.of(),
                address,
                address);
    }
}
