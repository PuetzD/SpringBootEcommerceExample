package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderConfirmationDelivery {
    default Optional<OrderConfirmationClaim> claim(
            UUID eventId, String orderNumber, Instant now, Instant claimExpiresAt) {
        return claim(eventId, orderNumber)
                ? Optional.of(new OrderConfirmationClaim(0))
                : Optional.empty();
    }

    @Deprecated(forRemoval = true)
    boolean claim(UUID eventId, String orderNumber);

    void markSent(UUID eventId, Instant sentAt);

    default void markFailed(
            UUID eventId, String diagnostic, Instant nextAttemptAt, boolean quarantine) {
        markFailed(eventId, diagnostic, nextAttemptAt);
    }

    @Deprecated(forRemoval = true)
    void markFailed(UUID eventId, String diagnostic, Instant nextAttemptAt);
}
