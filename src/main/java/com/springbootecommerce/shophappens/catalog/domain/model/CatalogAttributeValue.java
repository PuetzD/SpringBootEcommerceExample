package com.springbootecommerce.shophappens.catalog.domain.model;

import java.util.Objects;

public final class CatalogAttributeValue {
    private final String code;
    private final String label;
    private boolean active = true;

    CatalogAttributeValue(String code, String label) {
        this.code = normalizeCode(code);
        this.label = Objects.requireNonNull(label, "label").strip();
        if (this.label.isBlank()) throw new IllegalArgumentException("Label must not be blank");
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public boolean active() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    static String normalizeCode(String value) {
        String normalized =
                Objects.requireNonNull(value, "code").strip().toLowerCase().replace(' ', '-');
        if (normalized.isBlank() || !normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("Code must be a lowercase slug");
        }
        return normalized;
    }
}
