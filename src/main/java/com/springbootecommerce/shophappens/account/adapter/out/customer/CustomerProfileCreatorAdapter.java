package com.springbootecommerce.shophappens.account.adapter.out.customer;

import com.springbootecommerce.shophappens.account.application.port.out.CreateCustomerProfilePort;
import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.customer.application.port.in.CreateCustomerProfileUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import org.springframework.stereotype.Component;

@Component
final class CustomerProfileCreatorAdapter implements CreateCustomerProfilePort {
    private final CreateCustomerProfileUseCase profiles;

    CustomerProfileCreatorAdapter(CreateCustomerProfileUseCase profiles) {
        this.profiles = profiles;
    }

    @Override
    public CustomerReference create(AccountId accountId) {
        return profiles.create(new ExternalAccountId(accountId.value()));
    }
}
