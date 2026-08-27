package com.springbootecommerce.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordConfigurationTest {

    private final PasswordConfiguration passwordConfiguration = new PasswordConfiguration();

    @Test
    void encodesPasswordsWithBcryptIdentifier() {
        var passwordHash = passwordConfiguration.passwordEncoder().encode("password");

        assertThat(passwordHash).startsWith("{bcrypt}");
    }

    @Test
    void matchesCorrectPassword() {
        var passwordEncoder = passwordConfiguration.passwordEncoder();
        var passwordHash = passwordEncoder.encode("password");

        assertThat(passwordEncoder.matches("password", passwordHash)).isTrue();
        assertThat(passwordEncoder.matches("incorrect-password", passwordHash)).isFalse();
    }
}
