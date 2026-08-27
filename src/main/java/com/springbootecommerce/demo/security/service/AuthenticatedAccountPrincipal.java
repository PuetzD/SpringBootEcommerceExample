package com.springbootecommerce.demo.security.service;

import java.util.List;
import java.util.Objects;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class AuthenticatedAccountPrincipal extends User {
    private final Long accountId;

    public AuthenticatedAccountPrincipal(
            Long accountId, String email, String passwordHash, boolean enabled, String authority) {
        super(
                email,
                passwordHash,
                enabled,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(authority)));
        this.accountId = Objects.requireNonNull(accountId);
    }

    public Long accountId() {
        return accountId;
    }
}
