package com.springbootecommerce.shophappens.account.application.port.in;

import java.io.Serializable;

public record AccountReference(long value) implements Serializable {
    public AccountReference {
        if (value < 1) throw new IllegalArgumentException("Account ID must be positive");
    }
}
