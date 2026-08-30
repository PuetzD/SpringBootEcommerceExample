package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface CustomerCartGateway {
    CheckoutCart load(CustomerId customerId);

    void clear(CustomerId customerId);
}
