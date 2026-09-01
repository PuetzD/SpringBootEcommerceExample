package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategoryAdminSearch(int page, int size) {
    public CategoryAdminSearch {
        if (page < 0) throw new IllegalArgumentException("Page must not be negative");
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}
