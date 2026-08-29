package com.springbootecommerce.shophappens.cart.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record GuestCartReference(UUID value) {
    public static final String SESSION_ATTRIBUTE = "GUEST_CART_ID";

    public GuestCartReference {
        Objects.requireNonNull(value);
    }
}
