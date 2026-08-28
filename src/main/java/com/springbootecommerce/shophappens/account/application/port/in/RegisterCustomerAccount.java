package com.springbootecommerce.shophappens.account.application.port.in;

public record RegisterCustomerAccount(String email, String rawPassword) {
    public RegisterCustomerAccount {
        if (rawPassword == null || rawPassword.length() < 12) {
            throw new IllegalArgumentException("Password must contain at least 12 characters");
        }
    }
}
