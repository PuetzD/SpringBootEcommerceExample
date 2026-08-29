package com.springbootecommerce.shophappens.cart.domain.exception;

public final class ConcurrentCartModificationException extends RuntimeException {
    public ConcurrentCartModificationException(String message) {
        super(message);
    }
}
