package com.springbootecommerce.shophappens.catalog.application.port.in;

public record ProductRevision(long value) {
    public ProductRevision {
        if (value < 0) throw new IllegalArgumentException("Product revision must not be negative");
    }
}
