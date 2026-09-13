package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

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
@Table(name = "order_confirmation_delivery")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderConfirmationDeliveryJpaEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "order_number", nullable = false, length = 32)
    private String orderNumber;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 200)
    private String lastError;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static OrderConfirmationDeliveryJpaEntity create(
            UUID eventId, String orderNumber, Instant now) {
        var entity = new OrderConfirmationDeliveryJpaEntity();
        entity.eventId = eventId;
        entity.orderNumber = orderNumber;
        entity.status = "CLAIMED";
        entity.nextAttemptAt = now;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }
}
