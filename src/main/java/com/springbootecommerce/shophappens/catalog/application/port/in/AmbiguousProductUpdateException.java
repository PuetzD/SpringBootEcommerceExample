package com.springbootecommerce.shophappens.catalog.application.port.in;

public final class AmbiguousProductUpdateException extends RuntimeException {
    public AmbiguousProductUpdateException() {
        super("Select a variant to change commercial details");
    }
}
