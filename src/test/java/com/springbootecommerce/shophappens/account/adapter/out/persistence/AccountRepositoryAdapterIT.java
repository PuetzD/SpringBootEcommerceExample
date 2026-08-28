package com.springbootecommerce.shophappens.account.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

class AccountRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired AccountRepository accounts;
    @Autowired JdbcTemplate jdbc;

    @Test
    void findsExistingAccountByCanonicalEmail() {
        String seed = "query-" + UUID.randomUUID() + "@example.com";
        jdbc.update(
                "insert into account (email, password_hash, role) values (?, ?, ?)",
                seed,
                "{noop}x",
                "CUSTOMER");

        assertThat(accounts.findByEmail(new Email("  " + seed.toUpperCase() + " "))).isPresent();
    }

    @Test
    void databaseRejectsCanonicalDuplicates() {
        String seed = "dup-" + UUID.randomUUID() + "@example.com";
        jdbc.update(
                "insert into account (email, password_hash, role) values (?, ?, ?)",
                seed,
                "{noop}x",
                "CUSTOMER");

        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "insert into account (email, password_hash, role) values (?, ?, ?)",
                                        seed.toUpperCase(),
                                        "{noop}x",
                                        "CUSTOMER"))
                .isInstanceOf(DataAccessException.class);
    }
}
