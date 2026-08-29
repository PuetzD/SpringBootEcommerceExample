package com.springbootecommerce.shophappens.cart.domain.model;

public record ProductId(long value) {
    public ProductId {
        if (value < 1) throw new IllegalArgumentException("Product ID must be positive");
    }
}
