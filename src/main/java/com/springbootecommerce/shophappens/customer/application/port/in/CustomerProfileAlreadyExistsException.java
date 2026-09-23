package com.springbootecommerce.shophappens.customer.application.port.in;

public class CustomerProfileAlreadyExistsException extends RuntimeException {
    public CustomerProfileAlreadyExistsException(ExternalAccountId accountId) {
        super("A customer profile for account " + accountId.value() + " already exists");
    }
}
