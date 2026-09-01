package com.springbootecommerce.shophappens.catalog.application.port.in;

public class StaleCategoryRevisionException extends RuntimeException {
    private final CategoryReference category;
    private final CategoryRevision expectedRevision;

    public StaleCategoryRevisionException(
            CategoryReference category, CategoryRevision expectedRevision) {
        super("Category revision is stale: " + category.value());
        this.category = category;
        this.expectedRevision = expectedRevision;
    }

    public CategoryReference category() {
        return category;
    }

    public CategoryRevision expectedRevision() {
        return expectedRevision;
    }
}
