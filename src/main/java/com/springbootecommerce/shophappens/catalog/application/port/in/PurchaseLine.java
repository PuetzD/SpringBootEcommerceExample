package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.Objects;

public record PurchaseLine(ProductReference product, int quantity) {
    public PurchaseLine {
        Objects.requireNonNull(product);
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
    }
}
