package com.springbootecommerce.shophappens.administration.web.api;

import java.time.Instant;

public record OrderConfirmationDeliveryResponse(
        String status, Integer failedAttempts, String lastError, Instant nextTry, Instant sentAt) {
    public OrderConfirmationDeliveryResponse(String status) {
        this(status, 0, null, null, null);
    }
}
