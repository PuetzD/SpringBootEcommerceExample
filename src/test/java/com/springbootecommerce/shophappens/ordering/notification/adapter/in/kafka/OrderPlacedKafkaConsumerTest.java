package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import static org.mockito.Mockito.verify;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.SendOrderConfirmationUseCase;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Instant;
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
    @Mock SendOrderConfirmationUseCase confirmations;

    @Test
    void delegatesTheDeserializedEventToTheUseCase() throws Exception {
        var event = event();
        var consumer = consumer();

        consumer.consume(
                new ConsumerRecord<>(
                        OrderPlacedIntegrationEvent.EVENT_TYPE,
                        0,
                        0,
                        "order",
                        new ObjectMapper().writeValueAsString(event)));

        verify(confirmations).send(event);
    }

    @Test
    void rejectsMalformedJsonWithoutCallingTheUseCase() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () ->
                                consumer()
                                        .consume(
                                                new ConsumerRecord<>(
                                                        OrderPlacedIntegrationEvent.EVENT_TYPE,
                                                        0,
                                                        0,
                                                        "order",
                                                        "not-json")))
                .isInstanceOf(IllegalArgumentException.class);
        org.mockito.Mockito.verifyNoInteractions(confirmations);
    }

    private OrderPlacedKafkaConsumer consumer() {
        return new OrderPlacedKafkaConsumer(new ObjectMapper(), confirmations);
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
