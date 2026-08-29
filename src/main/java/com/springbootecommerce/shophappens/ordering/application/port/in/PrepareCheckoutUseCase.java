package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public interface PrepareCheckoutUseCase {
    CheckoutPreparation prepare(CustomerReference customer);
}
