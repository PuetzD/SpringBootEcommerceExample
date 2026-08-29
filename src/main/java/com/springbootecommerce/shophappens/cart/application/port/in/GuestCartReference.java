package com.springbootecommerce.shophappens.cart.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record GuestCartReference(UUID value) {
    public GuestCartReference {
        Objects.requireNonNull(value);
    }
}
