package com.springbootecommerce.demo.account.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.demo.account.domain.Role;
import com.springbootecommerce.demo.integration.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class JpaAccountAuthenticationQueryIT extends PostgresIntegrationTest {

  @Autowired private AccountAuthenticationQuery accountAuthenticationQuery;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void findsAccountByEmail() {
    var email = "customer@example.com";
    var passwordHash = "{bcrypt}password-hash";
    jdbcTemplate.update(
        "INSERT INTO account (email, password_hash, role, enabled) VALUES (?, ?, ?, ?)",
        email,
        passwordHash,
        Role.CUSTOMER.name(),
        true);

    var account = accountAuthenticationQuery.findByEmail(email);

    assertThat(account)
        .isEqualTo(new AuthenticatedAccount(email, passwordHash, Role.CUSTOMER, true));
  }
}
