package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategoryReference(long value) {
    public CategoryReference {
        if (value < 1) throw new IllegalArgumentException("Category reference must be positive");
    }
}
