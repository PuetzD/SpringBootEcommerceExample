package com.springbootecommerce.shophappens.customer.application.port.in;

import java.time.Instant;

public record CustomerAdminSearch(
        int page, int size, String query, Instant createdFrom, Instant createdBefore) {
    public CustomerAdminSearch(int page, int size, String query) {
        this(page, size, query, null, null);
    }

    public CustomerAdminSearch {
        if (page < 0) throw new IllegalArgumentException("Page must not be negative");
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        query = query == null || query.isBlank() ? null : query.strip();
        if (createdFrom != null && createdBefore != null && !createdFrom.isBefore(createdBefore)) {
            throw new IllegalArgumentException("Creation range start must be before end");
        }
    }
}
