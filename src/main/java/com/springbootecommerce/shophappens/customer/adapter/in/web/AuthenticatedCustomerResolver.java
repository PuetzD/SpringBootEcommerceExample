package com.springbootecommerce.shophappens.customer.adapter.in.web;

import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class AuthenticatedCustomerResolver {

    private final AuthenticatedAccountIdentity authenticatedAccount;
    private final CustomerReferenceQuery customers;

    public Optional<CustomerReference> resolve() {
        var accountId = new ExternalAccountId(authenticatedAccount.account().value());
        return customers.findByExternalAccountId(accountId);
    }
}
