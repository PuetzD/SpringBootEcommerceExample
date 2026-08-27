package com.springbootecommerce.shophappens.account.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.shophappens.account.domain.Role;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JpaAccountAuthenticationQueryIT extends AbstractIntegrationTest {

    @Autowired private AccountAuthenticationQuery accountAuthenticationQuery;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void findsAccountByEmail() {
        var email = "query-" + UUID.randomUUID() + "@example.com";
        var passwordHash = "{bcrypt}password-hash";
        jdbcTemplate.update(
                "INSERT INTO account (email, password_hash, role, enabled) VALUES (?, ?, ?, ?)",
                email,
                passwordHash,
                Role.CUSTOMER.name(),
                true);

        var id =
                jdbcTemplate.queryForObject(
                        "SELECT id FROM account WHERE email = ?", Long.class, email);
        var account = accountAuthenticationQuery.findByEmail(email);

        assertThat(account.id()).isEqualTo(id);
        assertThat(account.email()).isEqualTo(email);
        assertThat(account.passwordHash()).isEqualTo(passwordHash);
        assertThat(account.role()).isEqualTo(Role.CUSTOMER);
        assertThat(account.enabled()).isTrue();
    }

    @Test
    void resolvesNormalizedStoredEmail() {
        var email = "norm-" + UUID.randomUUID() + "@example.com";
        jdbcTemplate.update(
                "INSERT INTO account (email, password_hash, role, enabled) VALUES (?, ?, ?, ?)",
                email,
                "{bcrypt}password-hash",
                Role.CUSTOMER.name(),
                true);

        var account = accountAuthenticationQuery.findByEmail(email);

        assertThat(account).isNotNull();
        assertThat(account.email()).isEqualTo(email);
    }

    @Test
    void throwsWhenEmailDoesNotExist() {
        assertThatThrownBy(() -> accountAuthenticationQuery.findByEmail("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
