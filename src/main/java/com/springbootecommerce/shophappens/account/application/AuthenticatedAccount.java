package com.springbootecommerce.shophappens.account.application;

public record AuthenticatedAccount(
        Long id, String email, String passwordHash, String authority, boolean enabled) {}
