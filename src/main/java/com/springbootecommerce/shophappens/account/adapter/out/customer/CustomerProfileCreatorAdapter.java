package com.springbootecommerce.shophappens.account.adapter.out.customer;

import com.springbootecommerce.shophappens.account.application.port.out.CreateCustomerProfilePort;
import com.springbootecommerce.shophappens.customer.application.port.in.CreateCustomerProfileUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class CustomerProfileCreatorAdapter implements CreateCustomerProfilePort {
    private final CreateCustomerProfileUseCase profiles;

    @Override
    public void create(AccountId accountId) {
        profiles.create(new ExternalAccountId(accountId.value()));
    }
}
