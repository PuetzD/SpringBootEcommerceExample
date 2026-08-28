package com.springbootecommerce.shophappens.account.domain.model;

import java.util.Objects;
import java.util.Optional;

public final class Account {
    private final AccountId id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final Role role;
    private boolean enabled;

    private Account(
            AccountId id, Email email, PasswordHash passwordHash, Role role, boolean enabled) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.enabled = enabled;
    }

    public static Account restore(
            AccountId id, Email email, PasswordHash passwordHash, Role role, boolean enabled) {
        return new Account(Objects.requireNonNull(id), email, passwordHash, role, enabled);
    }

    public static Account registerCustomer(Email email, PasswordHash passwordHash) {
        return new Account(null, email, passwordHash, Role.CUSTOMER, true);
    }

    public Optional<AccountId> id() {
        return Optional.ofNullable(id);
    }

    public Email email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public boolean enabled() {
        return enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
