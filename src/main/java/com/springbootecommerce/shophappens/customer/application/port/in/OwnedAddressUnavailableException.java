package com.springbootecommerce.shophappens.customer.application.port.in;

public final class OwnedAddressUnavailableException extends RuntimeException {
    public OwnedAddressUnavailableException(String message) {
        super(message);
    }
}
