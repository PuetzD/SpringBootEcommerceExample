package com.springbootecommerce.shophappens.catalog.domain.model;

import java.util.Locale;

public record Sku(String value) {
    public Sku {
        if (value == null) throw new IllegalArgumentException("SKU must not be null");
        value = value.strip().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z0-9_.-]{1,50}")) {
            throw new IllegalArgumentException("SKU format is invalid");
        }
    }
}
