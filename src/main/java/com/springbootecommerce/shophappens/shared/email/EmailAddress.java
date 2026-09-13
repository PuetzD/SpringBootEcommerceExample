package com.springbootecommerce.shophappens.shared.email;

import java.util.Locale;

public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null) throw new IllegalArgumentException("Email address must not be null");
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isBlank() || value.length() > 254 || !value.contains("@")) {
            throw new IllegalArgumentException("Email address is invalid");
        }
    }
}
