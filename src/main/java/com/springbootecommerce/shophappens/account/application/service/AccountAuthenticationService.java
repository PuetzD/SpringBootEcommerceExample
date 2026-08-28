package com.springbootecommerce.shophappens.account.application.service;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticateAccountQuery;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationAccount;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationRole;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.Role;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class AccountAuthenticationService implements AuthenticateAccountQuery {

    private final AccountRepository accounts;

    public AccountAuthenticationService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationAccount> findByEmail(String email) {
        return accounts.findByEmail(new Email(email)).map(this::toAuthenticationAccount);
    }

    private AuthenticationAccount toAuthenticationAccount(Account account) {
        AccountId accountId =
                account.id()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Account was loaded without an identifier"));
        return new AuthenticationAccount(
                new AccountReference(accountId.value()),
                account.email().value(),
                account.passwordHash().value(),
                toAuthenticationRole(account.role()),
                account.enabled());
    }

    private AuthenticationRole toAuthenticationRole(Role role) {
        return switch (role) {
            case CUSTOMER -> AuthenticationRole.CUSTOMER;
            case ADMIN -> AuthenticationRole.ADMIN;
        };
    }
}
