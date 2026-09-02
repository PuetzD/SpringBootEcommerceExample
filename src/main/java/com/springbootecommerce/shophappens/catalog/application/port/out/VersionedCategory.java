package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.Category;

public record VersionedCategory(Category category, long revision) {
    public VersionedCategory {
        if (category == null) throw new NullPointerException("category");
        if (revision < 0)
            throw new IllegalArgumentException("Category revision must not be negative");
    }
}
