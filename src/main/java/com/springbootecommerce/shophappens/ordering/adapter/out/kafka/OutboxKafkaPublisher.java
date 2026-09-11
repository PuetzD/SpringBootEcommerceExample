package com.springbootecommerce.shophappens.ordering.adapter.out.kafka;

import com.springbootecommerce.shophappens.ordering.application.port.in.UpdateOutboxStatusUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox.PendingIntegrationEvent;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ordering.events.kafka.enabled", havingValue = "true")
public class OutboxKafkaPublisher {
    private static final int BATCH_SIZE = 100;

    private final IntegrationEventOutbox outbox;
    private final UpdateOutboxStatusUseCase statuses;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;

    @Autowired
    public OutboxKafkaPublisher(
            IntegrationEventOutbox outbox,
            UpdateOutboxStatusUseCase statuses,
            KafkaTemplate<String, String> kafka,
            Clock clock) {
        this.outbox = outbox;
        this.statuses = statuses;
        this.kafka = kafka;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${ordering.events.kafka.poll-delay:1000}")
    public void publishPending() {
        for (PendingIntegrationEvent event : outbox.pending(BATCH_SIZE)) {
            try {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(
                                event.eventType(), event.aggregateKey(), event.payload());
                record.headers()
                        .add("event-type", event.eventType().getBytes(StandardCharsets.UTF_8));
                record.headers()
                        .add(
                                "event-version",
                                eventVersion(event.eventType()).getBytes(StandardCharsets.UTF_8));
                record.headers()
                        .add(
                                "event-id",
                                event.eventId().toString().getBytes(StandardCharsets.UTF_8));
                kafka.send(record)
                        .whenComplete(
                                (result, exception) -> {
                                    if (exception == null) {
                                        statuses.markPublished(event.eventId(), Instant.now(clock));
                                    } else {
                                        Throwable cause =
                                                exception.getCause() == null
                                                        ? exception
                                                        : exception.getCause();
                                        statuses.markFailed(
                                                event.eventId(), cause.getClass().getSimpleName());
                                    }
                                });
            } catch (RuntimeException exception) {
                statuses.markFailed(event.eventId(), exception.getClass().getSimpleName());
            }
        }
    }

    private static String eventVersion(String eventType) {
        String prefix = "ordering.order-placed.v";
        if (!eventType.startsWith(prefix) || eventType.length() == prefix.length()) {
            throw new IllegalArgumentException("Unsupported outbox event type");
        }
        String version = eventType.substring(prefix.length());
        if (version.chars().anyMatch(character -> character < '0' || character > '9')) {
            throw new IllegalArgumentException("Unsupported outbox event type");
        }
        return version;
    }
}
