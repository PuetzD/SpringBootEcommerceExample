package com.springbootecommerce.shophappens.ordering.notification.application.port.in;

import java.time.Instant;

public class OrderConfirmationPendingException extends RuntimeException {
    private final Instant nextEligibleAt;

    public OrderConfirmationPendingException(Instant nextEligibleAt) {
        super("Order confirmation delivery is pending until " + nextEligibleAt);
        this.nextEligibleAt = nextEligibleAt;
    }

    public Instant nextEligibleAt() {
        return nextEligibleAt;
    }
}
