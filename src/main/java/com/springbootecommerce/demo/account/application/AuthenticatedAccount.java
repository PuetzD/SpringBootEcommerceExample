package com.springbootecommerce.demo.account.application;

import com.springbootecommerce.demo.account.domain.Role;

public record AuthenticatedAccount(String email, String passwordHash, Role role, boolean enabled) {}
