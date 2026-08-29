package com.springbootecommerce.shophappens.cart.domain.model;

public record Quantity(int value) {
    public Quantity {
        if (value < 1 || value > 999) {
            throw new IllegalArgumentException("Quantity must be between 1 and 999");
        }
    }

    public Quantity add(Quantity other) {
        return new Quantity(Math.addExact(value, other.value));
    }
}
