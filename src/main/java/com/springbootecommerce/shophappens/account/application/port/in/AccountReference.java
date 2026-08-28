package com.springbootecommerce.shophappens.account.application.port.in;

public record AccountReference(long value) {
    public AccountReference {
        if (value < 1) throw new IllegalArgumentException("Account ID must be positive");
    }
}
