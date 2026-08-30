package com.springbootecommerce.shophappens.ordering.adapter.out.kafka;

import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox.PendingIntegrationEvent;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ordering.events.kafka.enabled", havingValue = "true")
public class OutboxKafkaPublisher {
    private static final int BATCH_SIZE = 100;
    private static final String TOPIC = "ordering.order-placed.v1";

    private final IntegrationEventOutbox outbox;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock = Clock.systemUTC();

    @Scheduled(fixedDelayString = "${ordering.events.kafka.poll-delay:1000}")
    public void publishPending() {
        for (PendingIntegrationEvent event : outbox.pending(BATCH_SIZE)) {
            try {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, event.aggregateKey(), event.payload());
                record.headers()
                        .add("event-type", event.eventType().getBytes(StandardCharsets.UTF_8));
                record.headers()
                        .add(
                                "event-id",
                                event.eventId().toString().getBytes(StandardCharsets.UTF_8));
                kafka.send(record).get();
                outbox.markPublished(event.eventId(), Instant.now(clock));
            } catch (Exception exception) {
                outbox.markFailed(event.eventId(), exception.getMessage());
            }
        }
    }
}
