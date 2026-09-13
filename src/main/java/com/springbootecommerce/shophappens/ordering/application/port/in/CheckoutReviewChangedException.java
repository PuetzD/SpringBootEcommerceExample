package com.springbootecommerce.shophappens.ordering.application.port.in;

public final class CheckoutReviewChangedException extends RuntimeException {
    public CheckoutReviewChangedException() {
        super(
                "Your checkout review has changed or expired. Review the current details and try again.");
    }
}
