package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface ClearCustomerCartUseCase {
    void clear(CustomerId customer);
}
