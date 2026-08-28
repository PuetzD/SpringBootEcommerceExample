package com.springbootecommerce.shophappens.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Quantity {

    @Column(name = "quantity", nullable = false)
    private int value;

    protected Quantity() {}

    public Quantity(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Quantity quantity && value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
