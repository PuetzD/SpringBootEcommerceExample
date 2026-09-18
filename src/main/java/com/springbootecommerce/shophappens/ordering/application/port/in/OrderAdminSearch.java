package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.time.Instant;

public record OrderAdminSearch(
        int page, int size, String query, Instant placedFrom, Instant placedBefore) {
    public OrderAdminSearch(int page, int size, String query) {
        this(page, size, query, null, null);
    }

    public OrderAdminSearch {
        if (page < 0) throw new IllegalArgumentException("Page must not be negative");
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        query = query == null || query.isBlank() ? null : query.strip();
        if (placedFrom != null && placedBefore != null && !placedFrom.isBefore(placedBefore)) {
            throw new IllegalArgumentException("Placed range start must be before end");
        }
    }
}
