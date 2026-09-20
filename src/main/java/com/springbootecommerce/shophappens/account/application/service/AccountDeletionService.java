package com.springbootecommerce.shophappens.account.application.service;

import com.springbootecommerce.shophappens.account.application.port.in.AccountNotFoundException;
import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.DeleteCustomerAccountUseCase;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.application.port.out.CustomerForAccountPort;
import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerCartPort;
import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerProfilePort;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountDeletionService implements DeleteCustomerAccountUseCase {
    private final CustomerForAccountPort customers;
    private final RemoveCustomerCartPort carts;
    private final RemoveCustomerProfilePort profiles;
    private final AccountRepository accounts;

    @Override
    @Transactional
    public void delete(AccountReference reference) {
        AccountId accountId = new AccountId(reference.value());
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(reference));
        customers.find(accountId).ifPresent(carts::remove);
        profiles.remove(accountId);
        accounts.delete(accountId);
    }
}
