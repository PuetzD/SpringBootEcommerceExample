package com.springbootecommerce.shophappens.account.adapter.out.customer;

import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerProfilePort;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.customer.application.port.in.RemoveCustomerProfileUseCase;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CustomerProfileRemovalAdapter implements RemoveCustomerProfilePort {
    private final RemoveCustomerProfileUseCase profiles;

    @Override
    public void remove(AccountId accountId) {
        profiles.remove(new ExternalAccountId(accountId.value()));
    }
}
