package com.springbootecommerce.shophappens.catalog.domain.model;

public record CategoryId(long value) {
    public CategoryId {
        if (value < 1) throw new IllegalArgumentException("Category ID must be positive");
    }
}
