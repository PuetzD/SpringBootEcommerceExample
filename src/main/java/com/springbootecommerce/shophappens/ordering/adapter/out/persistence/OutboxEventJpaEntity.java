package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "integration_outbox")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OutboxEventJpaEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_key", nullable = false, length = 100)
    private String aggregateKey;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    static OutboxEventJpaEntity create(
            UUID eventId,
            String eventType,
            String aggregateKey,
            String payload,
            Instant createdAt) {
        var entity = new OutboxEventJpaEntity();
        entity.eventId = eventId;
        entity.eventType = eventType;
        entity.aggregateType = "Order";
        entity.aggregateKey = aggregateKey;
        entity.payload = payload;
        entity.createdAt = createdAt;
        return entity;
    }
}
