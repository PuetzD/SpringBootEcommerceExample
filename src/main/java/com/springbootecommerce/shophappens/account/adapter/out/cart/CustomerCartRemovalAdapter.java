package com.springbootecommerce.shophappens.account.adapter.out.cart;

import com.springbootecommerce.shophappens.account.application.port.out.RemoveCustomerCartPort;
import com.springbootecommerce.shophappens.cart.application.port.in.RemoveCustomerCartUseCase;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CustomerCartRemovalAdapter implements RemoveCustomerCartPort {
    private final RemoveCustomerCartUseCase carts;

    @Override
    public void remove(CustomerId customerId) {
        carts.remove(customerId);
    }
}
