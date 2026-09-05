package com.springbootecommerce.shophappens.catalog.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class Category {
    private final CategoryId id;
    private String name;
    private String slug;

    private Category(CategoryId id, String name, String slug) {
        this.id = id;
        this.name = Objects.requireNonNull(name).strip();
        this.slug = Objects.requireNonNull(slug);
    }

    public static Category create(String name) {
        String normalizedName = normalizeName(name);
        return new Category(null, normalizedName, slugify(normalizedName));
    }

    public static Category restore(CategoryId id, String name, String slug) {
        return new Category(Objects.requireNonNull(id), name, Objects.requireNonNull(slug));
    }

    public static Category restore(CategoryId id, String name) {
        return restore(id, name, slugify(name));
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

    public void rename(String name) {
        String normalizedName = normalizeName(name);
        this.name = normalizedName;
        this.slug = slugify(normalizedName);
    }

    private static String slugify(String name) {
        return name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return name.strip();
    }
}
