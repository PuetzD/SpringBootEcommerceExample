package com.springbootecommerce.shophappens.customer.domain.model;

import java.util.Locale;

public record ContactEmail(String value) {
    public ContactEmail {
        if (value == null) throw new IllegalArgumentException("Contact email must not be null");
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isBlank() || value.length() > 254 || !value.contains("@")) {
            throw new IllegalArgumentException("Contact email is invalid");
        }
    }
}
