package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.OrderConfirmationEmailService;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "notifications.email.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class OrderPlacedKafkaConsumer {
    private final ObjectMapper objectMapper;
    private final OrderConfirmationEmailService emails;
    private final OrderConfirmationDelivery deliveries;
    private final Clock clock;

    @KafkaListener(
            topics = OrderPlacedIntegrationEvent.EVENT_TYPE,
            groupId = "${notifications.email.kafka.group:order-confirmation-email}")
    public void consume(ConsumerRecord<String, String> record) {
        OrderPlacedIntegrationEvent event = read(record.value());
        if (!deliveries.claim(event.eventId(), event.orderNumber())) return;
        try {
            emails.send(event);
            deliveries.markSent(event.eventId(), Instant.now(clock));
        } catch (RuntimeException exception) {
            deliveries.markFailed(event.eventId(), diagnostic(exception), Instant.now(clock));
            throw exception;
        }
    }

    private OrderPlacedIntegrationEvent read(String payload) {
        try {
            return objectMapper.readValue(payload, OrderPlacedIntegrationEvent.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid order placed event payload", exception);
        }
    }

    private static String diagnostic(Throwable exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
