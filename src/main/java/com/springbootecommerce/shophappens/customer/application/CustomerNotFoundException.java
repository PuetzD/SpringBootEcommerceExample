package com.springbootecommerce.shophappens.customer.application;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(CustomerReference customer) {
        super("Customer " + customer.value() + " was not found");
    }
}
