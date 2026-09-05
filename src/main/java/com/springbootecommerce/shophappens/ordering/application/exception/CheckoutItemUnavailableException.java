package com.springbootecommerce.shophappens.ordering.application.exception;

public final class CheckoutItemUnavailableException extends RuntimeException {
    public CheckoutItemUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
