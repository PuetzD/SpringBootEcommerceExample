package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategoryRevision(long value) {
    public CategoryRevision {
        if (value < 0) throw new IllegalArgumentException("Category revision must not be negative");
    }
}
