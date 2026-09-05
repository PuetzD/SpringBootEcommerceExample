package com.springbootecommerce.shophappens.catalog.application.port.in;

public record ProductAdminSearch(int page, int size, String query, Boolean active) {
    public ProductAdminSearch {
        if (page < 0) throw new IllegalArgumentException("Page must not be negative");
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        query = query == null || query.isBlank() ? null : query.strip();
    }

    public ProductAdminSearch(int page, int size) {
        this(page, size, null, null);
    }
}
