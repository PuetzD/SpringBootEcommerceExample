package com.springbootecommerce.shophappens.cart.application.port.in;

public final class GuestCartConsumedException extends RuntimeException {
    public GuestCartConsumedException() {
        super(
                "Your guest cart has already been merged. Reload your cart before making another change.");
    }
}
