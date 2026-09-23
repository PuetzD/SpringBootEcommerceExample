package com.springbootecommerce.shophappens.account.adapter.out.customer;

import com.springbootecommerce.shophappens.account.application.port.out.CustomerForAccountPort;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CustomerForAccountAdapter implements CustomerForAccountPort {
    private final CustomerReferenceQuery customers;

    @Override
    public Optional<CustomerId> find(AccountId accountId) {
        return customers
                .findByExternalAccountId(new ExternalAccountId(accountId.value()))
                .map(reference -> new CustomerId(reference.value()));
    }
}
