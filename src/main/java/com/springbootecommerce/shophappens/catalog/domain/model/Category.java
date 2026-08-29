package com.springbootecommerce.shophappens.catalog.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class Category {
    private final CategoryId id;
    private final String name;
    private final String slug;

    private Category(CategoryId id, String name, String slug) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.slug = Objects.requireNonNull(slug);
    }

    public static Category create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return new Category(null, name, slugify(name));
    }

    public static Category restore(CategoryId id, String name) {
        return new Category(Objects.requireNonNull(id), name, slugify(name));
    }

    public Optional<CategoryId> id() {
        return Optional.ofNullable(id);
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }

    private static String slugify(String name) {
        return name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
