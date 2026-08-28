package com.springbootecommerce.shophappens.customer.domain.model;

public record AddressId(long value) {
    public AddressId {
        if (value < 1) throw new IllegalArgumentException("Address ID must be positive");
    }
}
