package com.springbootecommerce.shophappens.customer.application;

import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;

public class CustomerProfileAlreadyExistsException extends RuntimeException {
    public CustomerProfileAlreadyExistsException(ExternalAccountId accountId) {
        super("A customer profile for account " + accountId.value() + " already exists");
    }
}
