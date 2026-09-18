package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SpringDataOrderConfirmationDeliveryRepository
        extends JpaRepository<OrderConfirmationDeliveryJpaEntity, UUID> {
    @Query(
            value =
                    """
                    INSERT INTO order_confirmation_delivery
                        (event_id, order_number, status, attempt_count, next_attempt_at,
                         claim_expires_at, created_at, updated_at)
                    VALUES (:eventId, :orderNumber, 'CLAIMED', 0, :now, :claimExpiresAt, :now, :now)
                    ON CONFLICT (event_id) DO UPDATE
                    SET status = 'CLAIMED',
                        claim_expires_at = EXCLUDED.claim_expires_at,
                        updated_at = EXCLUDED.updated_at
                    WHERE (order_confirmation_delivery.status = 'FAILED'
                           AND order_confirmation_delivery.next_attempt_at <= EXCLUDED.updated_at)
                       OR (order_confirmation_delivery.status = 'CLAIMED'
                           AND order_confirmation_delivery.claim_expires_at <= EXCLUDED.updated_at)
                    RETURNING attempt_count
                    """,
            nativeQuery = true)
    Optional<Integer> claim(UUID eventId, String orderNumber, Instant now, Instant claimExpiresAt);
}
