package com.springbootecommerce.shophappens.account.application.service;

import com.springbootecommerce.shophappens.account.application.EmailAlreadyRegisteredException;
import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccountUseCase;
import com.springbootecommerce.shophappens.account.application.port.in.RegisteredCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.application.port.out.CreateCustomerProfilePort;
import com.springbootecommerce.shophappens.account.application.port.out.PasswordHasher;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountRegistrationService implements RegisterCustomerAccountUseCase {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final CreateCustomerProfilePort profiles;

    @Override
    @Transactional
    public RegisteredCustomerAccount register(RegisterCustomerAccount command) {
        Email email = new Email(command.email());
        if (accounts.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        PasswordHash passwordHash = passwordHasher.hash(command.rawPassword());
        Account saved = accounts.save(Account.registerCustomer(email, passwordHash));
        AccountId savedId =
                saved.id()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Account was persisted without an identifier"));
        return new RegisteredCustomerAccount(
                new AccountReference(savedId.value()), profiles.create(savedId));
    }
}
