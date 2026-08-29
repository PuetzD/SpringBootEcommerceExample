package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record CheckoutReference(UUID value) {
    public CheckoutReference {
        Objects.requireNonNull(value);
    }
}
