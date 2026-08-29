package com.springbootecommerce.shophappens.sharedkernel.identity;

public record AccountId(long value) {
    public AccountId {
        if (value < 1) throw new IllegalArgumentException("Account ID must be positive");
    }
}
