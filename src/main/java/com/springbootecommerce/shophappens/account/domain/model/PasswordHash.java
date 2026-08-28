package com.springbootecommerce.shophappens.account.domain.model;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Password hash must not be null or blank");
    }
}
