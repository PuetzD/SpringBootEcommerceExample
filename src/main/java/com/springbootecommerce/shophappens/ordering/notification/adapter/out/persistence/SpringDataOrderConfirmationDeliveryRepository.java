package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface SpringDataOrderConfirmationDeliveryRepository
        extends JpaRepository<OrderConfirmationDeliveryJpaEntity, UUID> {
    @Query(
            value =
                    """
                    INSERT INTO order_confirmation_delivery
                        (event_id, order_number, status, attempt_count, next_attempt_at,
                         claim_expires_at, claim_token, created_at, updated_at)
                    VALUES (:eventId, :orderNumber, 'CLAIMED', 0, :now, :claimExpiresAt, :claimToken, :now, :now)
                    ON CONFLICT (event_id) DO UPDATE
                    SET status = 'CLAIMED',
                        claim_expires_at = EXCLUDED.claim_expires_at,
                        claim_token = EXCLUDED.claim_token,
                        updated_at = EXCLUDED.updated_at
                    WHERE (order_confirmation_delivery.status = 'FAILED'
                           AND order_confirmation_delivery.next_attempt_at <= EXCLUDED.updated_at)
                       OR (order_confirmation_delivery.status = 'CLAIMED'
                           AND order_confirmation_delivery.claim_expires_at <= EXCLUDED.updated_at)
                    RETURNING attempt_count
                    """,
            nativeQuery = true)
    Optional<Integer> claim(
            UUID eventId, String orderNumber, UUID claimToken, Instant now, Instant claimExpiresAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            UPDATE OrderConfirmationDeliveryJpaEntity
            SET status = 'SENT', sentAt = :sentAt, updatedAt = :updatedAt
            WHERE eventId = :eventId AND claimToken = :claimToken AND status = 'CLAIMED'
            """)
    int markSent(UUID eventId, UUID claimToken, Instant sentAt, Instant updatedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            UPDATE OrderConfirmationDeliveryJpaEntity
            SET status = :status, attemptCount = attemptCount + 1, lastError = :diagnostic,
                nextAttemptAt = :nextAttemptAt, updatedAt = :updatedAt
            WHERE eventId = :eventId AND claimToken = :claimToken AND status = 'CLAIMED'
            """)
    int markFailed(
            UUID eventId,
            UUID claimToken,
            String status,
            String diagnostic,
            Instant nextAttemptAt,
            Instant updatedAt);

    Optional<OrderConfirmationDeliveryJpaEntity> findByOrderNumber(String orderNumber);
}
