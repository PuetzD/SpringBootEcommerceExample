package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.OrderConfirmationEmailService;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderPlacedKafkaConsumerTest {
    @Mock OrderConfirmationEmailService emails;
    @Mock OrderConfirmationDelivery deliveries;

    @Test
    void sendsAndMarksAClaimedOrderEvent() throws Exception {
        var event = event();
        when(deliveries.claim(event.eventId(), event.orderNumber())).thenReturn(true);
        var consumer = consumer();

        consumer.consume(
                new ConsumerRecord<>(
                        OrderPlacedIntegrationEvent.EVENT_TYPE,
                        0,
                        0,
                        "order",
                        new ObjectMapper().writeValueAsString(event)));

        verify(emails).send(event);
        verify(deliveries).markSent(event.eventId(), Instant.parse("2026-09-13T10:00:00Z"));
    }

    @Test
    void skipsAnAlreadyClaimedEvent() throws Exception {
        var event = event();
        when(deliveries.claim(event.eventId(), event.orderNumber())).thenReturn(false);

        consumer()
                .consume(
                        new ConsumerRecord<>(
                                OrderPlacedIntegrationEvent.EVENT_TYPE,
                                0,
                                0,
                                "order",
                                new ObjectMapper().writeValueAsString(event)));

        verify(emails, never()).send(any());
        verify(deliveries, never()).markSent(any(), any());
    }

    @Test
    void recordsAndPropagatesATransientEmailFailure() throws Exception {
        var event = event();
        when(deliveries.claim(event.eventId(), event.orderNumber())).thenReturn(true);
        doThrow(new IllegalStateException("smtp unavailable")).when(emails).send(event);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () ->
                                consumer()
                                        .consume(
                                                new ConsumerRecord<>(
                                                        OrderPlacedIntegrationEvent.EVENT_TYPE,
                                                        0,
                                                        0,
                                                        "order",
                                                        new ObjectMapper()
                                                                .writeValueAsString(event))))
                .isInstanceOf(IllegalStateException.class);
        verify(deliveries)
                .markFailed(
                        any(), org.mockito.ArgumentMatchers.contains("smtp unavailable"), any());
    }

    private OrderPlacedKafkaConsumer consumer() {
        return new OrderPlacedKafkaConsumer(
                new ObjectMapper(),
                emails,
                deliveries,
                Clock.fixed(Instant.parse("2026-09-13T10:00:00Z"), ZoneOffset.UTC));
    }

    private static OrderPlacedIntegrationEvent event() {
        return new OrderPlacedIntegrationEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.randomUUID(),
                "ORD-2026-100001",
                42L,
                "Ada",
                "ada@example.com",
                Instant.parse("2026-09-13T10:00:00Z"),
                new BigDecimal("39.98"),
                Currency.EUR,
                List.of(),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null));
    }
}
