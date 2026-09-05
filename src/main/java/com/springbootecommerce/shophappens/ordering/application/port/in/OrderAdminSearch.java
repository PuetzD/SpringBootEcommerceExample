package com.springbootecommerce.shophappens.ordering.application.port.in;

public record OrderAdminSearch(int page, int size, String query) {
    public OrderAdminSearch {
        if (page < 0) throw new IllegalArgumentException("Page must not be negative");
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        query = query == null || query.isBlank() ? null : query.strip();
    }
}
