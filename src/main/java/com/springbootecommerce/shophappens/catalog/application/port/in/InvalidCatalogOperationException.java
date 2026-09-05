package com.springbootecommerce.shophappens.catalog.application.port.in;

public class InvalidCatalogOperationException extends RuntimeException {
    public InvalidCatalogOperationException(String message) {
        super(message);
    }
}
