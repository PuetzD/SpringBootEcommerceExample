package com.springbootecommerce.shophappens.ordering.domain.model;

import java.util.UUID;

public record CheckoutId(UUID value) {
    public CheckoutId {
        if (value == null) throw new IllegalArgumentException("Checkout ID must not be null");
    }
}
