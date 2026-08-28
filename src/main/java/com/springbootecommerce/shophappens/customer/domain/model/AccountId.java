package com.springbootecommerce.shophappens.customer.domain.model;

public record AccountId(long value) {
    public AccountId {
        if (value < 1) throw new IllegalArgumentException("Account ID must be positive");
    }
}
