package com.springbootecommerce.shophappens.catalog.application.port.in;

public class CategoryNotFoundException extends RuntimeException {
    private final CategoryReference category;

    public CategoryNotFoundException(CategoryReference category) {
        super("Category not found: " + category.value());
        this.category = category;
    }

    public CategoryReference category() {
        return category;
    }
}
