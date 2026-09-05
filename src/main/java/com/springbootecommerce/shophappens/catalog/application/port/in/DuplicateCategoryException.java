package com.springbootecommerce.shophappens.catalog.application.port.in;

public class DuplicateCategoryException extends RuntimeException {
    private final String name;
    private final String slug;

    public DuplicateCategoryException(String name, String slug) {
        super("Category name or slug already exists: " + name + " (" + slug + ")");
        this.name = name;
        this.slug = slug;
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }
}
