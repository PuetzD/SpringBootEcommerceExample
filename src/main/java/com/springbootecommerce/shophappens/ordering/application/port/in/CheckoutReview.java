package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record CheckoutReview(CustomerId customer, List<CheckoutItem> items, Instant expiresAt) {
    public CheckoutReview {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(expiresAt, "expiresAt");
        items = List.copyOf(items);
    }
}
