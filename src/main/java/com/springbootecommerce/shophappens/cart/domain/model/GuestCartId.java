package com.springbootecommerce.shophappens.cart.domain.model;

import java.util.Objects;
import java.util.UUID;

public record GuestCartId(UUID value) {
    public GuestCartId {
        Objects.requireNonNull(value, "Guest cart ID must not be null");
    }

    public static GuestCartId random() {
        return new GuestCartId(UUID.randomUUID());
    }
}
