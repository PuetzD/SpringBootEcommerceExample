package com.springbootecommerce.shophappens.account.domain.model;

import java.util.Locale;

public record Email(String value) {
    public Email {
        if (value == null) throw new IllegalArgumentException("Email must not be null");
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isBlank() || value.length() > 254 || !value.contains("@")) {
            throw new IllegalArgumentException("Email is invalid");
        }
    }
}
