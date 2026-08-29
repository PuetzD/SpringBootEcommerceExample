package com.springbootecommerce.shophappens.cart.domain.model;

public record CustomerId(long value) {
    public CustomerId {
        if (value < 1) throw new IllegalArgumentException("Customer ID must be positive");
    }
}
