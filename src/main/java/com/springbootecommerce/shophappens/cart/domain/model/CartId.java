package com.springbootecommerce.shophappens.cart.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CartId(UUID value) {
    public CartId {
        Objects.requireNonNull(value, "Cart ID must not be null");
    }

    public static CartId random() {
        return new CartId(UUID.randomUUID());
    }
}
