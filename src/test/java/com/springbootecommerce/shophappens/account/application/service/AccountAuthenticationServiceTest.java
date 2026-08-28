package com.springbootecommerce.shophappens.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationAccount;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationRole;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import com.springbootecommerce.shophappens.account.domain.model.Role;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountAuthenticationServiceTest {
    @Mock AccountRepository accounts;
    @InjectMocks AccountAuthenticationService service;

    @Test
    void findsCustomerMappingRole() {
        when(accounts.findByEmail(new Email("customer@example.com")))
                .thenReturn(
                        Optional.of(
                                Account.restore(
                                        new AccountId(42L),
                                        new Email("customer@example.com"),
                                        new PasswordHash("{bcrypt}encoded"),
                                        Role.CUSTOMER,
                                        true)));

        Optional<AuthenticationAccount> result = service.findByEmail("customer@example.com");

        assertThat(result)
                .contains(
                        new AuthenticationAccount(
                                new AccountReference(42L),
                                "customer@example.com",
                                "{bcrypt}encoded",
                                AuthenticationRole.CUSTOMER,
                                true));
    }

    @Test
    void findsAdminMappingRole() {
        when(accounts.findByEmail(new Email("admin@example.com")))
                .thenReturn(
                        Optional.of(
                                Account.restore(
                                        new AccountId(99L),
                                        new Email("admin@example.com"),
                                        new PasswordHash("{bcrypt}encoded"),
                                        Role.ADMIN,
                                        true)));

        Optional<AuthenticationAccount> result = service.findByEmail("admin@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().role()).isEqualTo(AuthenticationRole.ADMIN);
    }

    @Test
    void returnsEmptyWhenRepositoryHasNoAccount() {
        when(accounts.findByEmail(new Email("missing@example.com"))).thenReturn(Optional.empty());

        assertThat(service.findByEmail("missing@example.com")).isEmpty();
    }
}
