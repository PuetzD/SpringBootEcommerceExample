package com.springbootecommerce.shophappens.customer.application.port.in;

public record AddressReference(long value) {
    public AddressReference {
        if (value < 1) throw new IllegalArgumentException("Address ID must be positive");
    }
}
