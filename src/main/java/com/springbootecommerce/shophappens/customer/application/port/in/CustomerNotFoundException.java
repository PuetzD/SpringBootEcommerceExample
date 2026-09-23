package com.springbootecommerce.shophappens.customer.application.port.in;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(CustomerReference customer) {
        super("Customer " + customer.value() + " was not found");
    }
}
