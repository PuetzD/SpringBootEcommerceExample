package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;

public interface CustomerCartGateway {
    CheckoutCart load(CustomerId customerId);
    void clear(CustomerId customerId);
}
