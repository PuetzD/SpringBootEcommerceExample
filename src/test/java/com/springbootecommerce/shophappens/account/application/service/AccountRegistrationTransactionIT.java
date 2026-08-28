package com.springbootecommerce.shophappens.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccountUseCase;
import com.springbootecommerce.shophappens.account.application.port.out.CreateCustomerProfilePort;
import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AccountRegistrationTransactionIT extends AbstractIntegrationTest {
    @Autowired RegisterCustomerAccountUseCase registration;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CreateCustomerProfilePort profiles;

    @Test
    void profileFailureRollsBackNewAccount() {
        String email = "rollback-profile@example.com";
        when(profiles.create(any(AccountId.class)))
                .thenThrow(new IllegalStateException("profile unavailable"));

        assertThatThrownBy(
                        () ->
                                registration.register(
                                        new RegisterCustomerAccount(email, "twelve-chars!")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from account where email = ?", Long.class, email))
                .isZero();
    }
}
