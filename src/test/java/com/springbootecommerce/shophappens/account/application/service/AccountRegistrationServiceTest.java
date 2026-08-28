package com.springbootecommerce.shophappens.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.in.RegisteredCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.application.port.out.CreateCustomerProfilePort;
import com.springbootecommerce.shophappens.account.application.port.out.PasswordHasher;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import com.springbootecommerce.shophappens.account.domain.model.Role;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountRegistrationServiceTest {
    @Mock AccountRepository accounts;
    @Mock PasswordHasher passwordHasher;
    @Mock CreateCustomerProfilePort profiles;
    @InjectMocks AccountRegistrationService service;

    @Test
    void registersAccountThenCreatesCustomerProfile() {
        when(accounts.existsByEmail(new Email("customer@example.com"))).thenReturn(false);
        when(passwordHasher.hash("plain-password")).thenReturn(new PasswordHash("{bcrypt}encoded"));
        when(accounts.save(any(Account.class)))
                .thenAnswer(
                        invocation ->
                                Account.restore(
                                        new AccountId(42L),
                                        new Email("customer@example.com"),
                                        new PasswordHash("{bcrypt}encoded"),
                                        Role.CUSTOMER,
                                        true));
        when(profiles.create(new AccountId(42L))).thenReturn(new CustomerReference(7L));

        RegisteredCustomerAccount result =
                service.register(
                        new RegisterCustomerAccount(" Customer@Example.com ", "plain-password"));

        assertThat(result.account()).isEqualTo(new AccountReference(42L));
        assertThat(result.customer()).isEqualTo(new CustomerReference(7L));
        InOrder order = inOrder(accounts, profiles);
        order.verify(accounts).save(any(Account.class));
        order.verify(profiles).create(new AccountId(42L));
    }
}
