package com.springbootecommerce.shophappens.catalog.application.port.in;

public class CategoryInUseException extends RuntimeException {
    private final CategoryReference category;

    public CategoryInUseException(CategoryReference category) {
        super("Category is still assigned to Products: " + category.value());
        this.category = category;
    }

    public CategoryReference category() {
        return category;
    }
}
