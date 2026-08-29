package com.springbootecommerce.shophappens.ordering.application.port.in;

public interface PrepareCheckoutUseCase {
    CheckoutPreparation prepare(CustomerReference customer);
}
