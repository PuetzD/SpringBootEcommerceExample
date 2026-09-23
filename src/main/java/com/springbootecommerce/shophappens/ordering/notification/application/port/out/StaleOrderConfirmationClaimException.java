package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.util.UUID;

public class StaleOrderConfirmationClaimException extends RuntimeException {
    public StaleOrderConfirmationClaimException(UUID eventId) {
        super("Order confirmation claim no longer owns event " + eventId);
    }
}
