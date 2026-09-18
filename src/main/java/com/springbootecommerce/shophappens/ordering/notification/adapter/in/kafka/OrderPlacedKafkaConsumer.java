package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.SendOrderConfirmationUseCase;
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
    private final SendOrderConfirmationUseCase confirmations;

    @KafkaListener(
            topics = OrderPlacedIntegrationEvent.EVENT_TYPE,
            containerFactory = "orderConfirmationKafkaListenerContainerFactory",
            properties = {"enable.auto.commit=false", "max.poll.interval.ms=600000"},
            groupId = "${notifications.email.kafka.group:order-confirmation-email}")
    public void consume(ConsumerRecord<String, String> record) {
        confirmations.send(read(record.value()));
    }

    private OrderPlacedIntegrationEvent read(String payload) {
        try {
            return objectMapper.readValue(payload, OrderPlacedIntegrationEvent.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid order placed event payload", exception);
        }
    }
}
