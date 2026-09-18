package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderConfirmationDelivery {
    Optional<OrderConfirmationClaim> claim(
            UUID eventId, String orderNumber, Instant now, Instant claimExpiresAt);

    void markSent(UUID eventId, Instant sentAt);

    void markFailed(UUID eventId, String diagnostic, Instant nextAttemptAt, boolean quarantine);
}
