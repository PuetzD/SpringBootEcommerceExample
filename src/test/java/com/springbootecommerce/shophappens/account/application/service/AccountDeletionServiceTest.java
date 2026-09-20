package com.springbootecommerce.shophappens.account.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountNotFoundException;
import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.application.port.out.CustomerForAccountPort;
import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerCartPort;
import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerProfilePort;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import com.springbootecommerce.shophappens.account.domain.model.Role;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {
    @Mock AccountRepository accounts;
    @Mock CustomerForAccountPort customers;
    @Mock RemoveCustomerCartPort carts;
    @Mock RemoveCustomerProfilePort profiles;
    @InjectMocks AccountDeletionService service;

    @Test
    void removesCurrentCustomerStateBeforeTheAccount() {
        AccountId accountId = new AccountId(42L);
        CustomerId customerId = new CustomerId(7L);
        when(accounts.findById(accountId)).thenReturn(Optional.of(account(accountId)));
        when(customers.find(accountId)).thenReturn(Optional.of(customerId));

        service.delete(new AccountReference(42L));

        InOrder order = inOrder(accounts, customers, carts, profiles);
        order.verify(accounts).findById(accountId);
        order.verify(customers).find(accountId);
        order.verify(carts).remove(customerId);
        order.verify(profiles).remove(accountId);
        order.verify(accounts).delete(accountId);
    }

    @Test
    void removesProfileAndAccountWhenCustomerStateIsAbsent() {
        AccountId accountId = new AccountId(42L);
        when(accounts.findById(accountId)).thenReturn(Optional.of(account(accountId)));
        when(customers.find(accountId)).thenReturn(Optional.empty());

        service.delete(new AccountReference(42L));

        verify(carts, never()).remove(new CustomerId(7L));
        verify(profiles).remove(accountId);
        verify(accounts).delete(accountId);
    }

    @Test
    void missingAccountStopsBeforeCleanup() {
        when(accounts.findById(new AccountId(42L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(new AccountReference(42L)))
                .isInstanceOf(AccountNotFoundException.class);

        verify(customers, never()).find(new AccountId(42L));
        verify(profiles, never()).remove(new AccountId(42L));
        verify(accounts, never()).delete(new AccountId(42L));
    }

    private static Account account(AccountId id) {
        return Account.restore(
                id, new Email("ada@example.com"), new PasswordHash("hash"), Role.CUSTOMER, true);
    }
}
