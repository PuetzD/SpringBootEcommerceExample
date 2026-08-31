package com.springbootecommerce.shophappens.security.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class AuthenticatedCustomerIdentityTest {
    @Mock private AuthenticatedAccountIdentity authenticatedAccount;
    @Mock private CustomerReferenceQuery customerQuery;
    @InjectMocks private AuthenticatedCustomerIdentity currentCustomer;

    @Test
    void givenAuthenticatedAccountWithCustomer_returnsCustomerReference() {
        // Arrange
        long accountId = 123L;
        long customerId = 234L;
        var externalAccountId = new ExternalAccountId(accountId);
        when(authenticatedAccount.account()).thenReturn(new AccountReference(accountId));
        when(customerQuery.findByExternalAccountId(externalAccountId))
                .thenReturn(Optional.of(new CustomerReference(customerId)));
        // Act
        var result = currentCustomer.current();
        // Assert
        assertThat(result).isPresent().contains(new CustomerReference(customerId));
        verify(customerQuery).findByExternalAccountId(externalAccountId);
    }

    @Test
    void givenAuthenticatedAccountWithoutCustomer_returnsEmpty() {
        // Arrange
        long accountId = 123L;
        var externalAccountId = new ExternalAccountId(accountId);
        when(authenticatedAccount.account()).thenReturn(new AccountReference(accountId));
        when(customerQuery.findByExternalAccountId(externalAccountId)).thenReturn(Optional.empty());
        // Act
        var result = currentCustomer.current();
        // Assert
        assertThat(result).isEmpty();
        verify(customerQuery).findByExternalAccountId(externalAccountId);
    }

    @Test
    void givenAnonymousRequest_returnsEmptyWithoutQuery() {
        // Arrange
        when(authenticatedAccount.account())
                .thenThrow(new IllegalStateException("Not authenticated"));
        // Act
        var result = currentCustomer.current();
        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(customerQuery);
    }
}
