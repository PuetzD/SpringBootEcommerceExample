package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.time.Instant;
import java.util.UUID;

public sealed interface OrderConfirmationClaim {
    record Acquired(int failedAttempts, UUID claimToken) implements OrderConfirmationClaim {}

    record Pending(Instant nextEligibleAt) implements OrderConfirmationClaim {}

    enum Terminal implements OrderConfirmationClaim {
        SENT,
        QUARANTINED
    }
}
