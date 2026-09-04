package com.springbootecommerce.shophappens.customer.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticatedCustomerResolverTest {
    @Mock AuthenticatedAccountIdentity authenticatedAccount;
    @Mock CustomerReferenceQuery customers;
    @InjectMocks AuthenticatedCustomerResolver resolver;

    @Test
    void resolvesTheCustomerForTheAuthenticatedAccount() {
        when(authenticatedAccount.account()).thenReturn(new AccountReference(42L));
        when(customers.findByExternalAccountId(new ExternalAccountId(42L)))
                .thenReturn(Optional.of(new CustomerReference(7L)));

        assertThat(resolver.resolve()).contains(new CustomerReference(7L));
    }

    @Test
    void reportsEmptyWhenTheAccountHasNoCustomerProfile() {
        when(authenticatedAccount.account()).thenReturn(new AccountReference(42L));
        when(customers.findByExternalAccountId(new ExternalAccountId(42L)))
                .thenReturn(Optional.empty());

        assertThat(resolver.resolve()).isEmpty();
    }
}
