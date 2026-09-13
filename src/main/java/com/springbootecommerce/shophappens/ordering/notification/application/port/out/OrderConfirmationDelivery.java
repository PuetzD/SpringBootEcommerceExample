package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface OrderConfirmationDelivery {
    boolean claim(UUID eventId, String orderNumber);

    void markSent(UUID eventId, Instant sentAt);

    void markFailed(UUID eventId, String diagnostic, Instant nextAttemptAt);
}
