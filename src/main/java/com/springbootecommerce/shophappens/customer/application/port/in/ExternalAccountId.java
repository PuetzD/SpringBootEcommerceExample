package com.springbootecommerce.shophappens.customer.application.port.in;

public record ExternalAccountId(long value) {
    public ExternalAccountId {
        if (value < 1) throw new IllegalArgumentException("Account ID must be positive");
    }
}
