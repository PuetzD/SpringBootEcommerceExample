package com.springbootecommerce.shophappens.security.service;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedCustomerIdentity implements CurrentCustomerIdentity {
    private final AuthenticatedAccountIdentity authenticatedAccount;
    private final CustomerReferenceQuery customerQuery;

    public AuthenticatedCustomerIdentity(
            AuthenticatedAccountIdentity authenticatedAccount,
            CustomerReferenceQuery customerQuery) {
        this.authenticatedAccount = authenticatedAccount;
        this.customerQuery = customerQuery;
    }

    @Override
    public Optional<CustomerReference> current() {
        AccountReference account;
        try {
            account = authenticatedAccount.account();
        } catch (IllegalStateException e) {
            // Not authenticated
            return Optional.empty();
        }
        var externalAccountId = new ExternalAccountId(account.value());
        return customerQuery.findByExternalAccountId(externalAccountId);
    }
}
