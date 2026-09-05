package com.springbootecommerce.shophappens.ordering.application.exception;

public final class CheckoutAddressUnavailableException extends RuntimeException {
    public CheckoutAddressUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
