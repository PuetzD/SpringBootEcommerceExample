package com.springbootecommerce.shophappens.customer.domain.exception;

public class AddressNotOwnedException extends RuntimeException {
    public AddressNotOwnedException(String message) {
        super(message);
    }
}
