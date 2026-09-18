package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface OrderConfirmationDelivery {
    OrderConfirmationClaim claim(
            UUID eventId, String orderNumber, UUID claimToken, Instant now, Instant claimExpiresAt);

    void markSent(UUID eventId, UUID claimToken, Instant sentAt);

    void markFailed(
            UUID eventId,
            UUID claimToken,
            String diagnostic,
            Instant nextAttemptAt,
            boolean quarantine);
}
