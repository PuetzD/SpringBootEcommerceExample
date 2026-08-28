package com.springbootecommerce.shophappens.customer.application.port.in;

public record CustomerReference(long value) {
    public CustomerReference {
        if (value < 1) throw new IllegalArgumentException("Customer ID must be positive");
    }
}
