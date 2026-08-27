package com.springbootecommerce.shophappens.account.application;

import com.springbootecommerce.shophappens.account.domain.Role;

public record AuthenticatedAccount(
        Long id, String email, String passwordHash, Role role, boolean enabled) {}
