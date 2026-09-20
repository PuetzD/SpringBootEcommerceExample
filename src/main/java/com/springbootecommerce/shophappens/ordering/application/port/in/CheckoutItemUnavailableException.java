package com.springbootecommerce.shophappens.ordering.application.port.in;

public final class CheckoutItemUnavailableException extends RuntimeException {
    public CheckoutItemUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
