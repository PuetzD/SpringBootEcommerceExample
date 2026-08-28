package com.springbootecommerce.shophappens.account.application.port.in;

public record AuthenticationAccount(
        AccountReference account,
        String email,
        String passwordHash,
        AuthenticationRole role,
        boolean enabled) {}
