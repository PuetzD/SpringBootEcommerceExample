package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface PrepareCheckoutUseCase {
    CheckoutPreparation prepare(CustomerId customer);
}
