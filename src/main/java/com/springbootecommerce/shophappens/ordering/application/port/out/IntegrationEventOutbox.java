package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.event.OrderPlaced;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IntegrationEventOutbox {
    void append(OrderPlaced event);

    List<PendingIntegrationEvent> pending(int batchSize);

    void markPublished(UUID eventId, Instant publishedAt);

    void markFailed(UUID eventId, String error);

    record PendingIntegrationEvent(
            UUID eventId, String eventType, String aggregateKey, String payload) {}
}
