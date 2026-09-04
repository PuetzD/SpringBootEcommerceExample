package com.springbootecommerce.shophappens.cart.adapter.in.web;

import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public final class CartOwnerResolver {

    private final AuthenticatedAccountIdentity authenticatedAccount;
    private final CustomerReferenceQuery customers;

    public Optional<CustomerReference> resolve() {
        try {
            var accountId = new ExternalAccountId(authenticatedAccount.account().value());
            return customers.findByExternalAccountId(accountId);
        } catch (IllegalStateException anonymous) {
            return Optional.empty();
        }
    }
}
