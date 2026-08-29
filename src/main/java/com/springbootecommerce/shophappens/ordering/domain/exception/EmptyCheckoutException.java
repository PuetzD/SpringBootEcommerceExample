package com.springbootecommerce.shophappens.ordering.domain.exception;

public final class EmptyCheckoutException extends RuntimeException {
    public EmptyCheckoutException() {
        super("Checkout must contain at least one item");
    }
}
