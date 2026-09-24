package com.springbootecommerce.shophappens.ordering.notification.application.port.in;

import java.time.Instant;

public record OrderConfirmationDeliveryView(
        String status, int failedAttempts, String lastError, Instant nextRetryAt, Instant sentAt) {}
