package com.springbootecommerce.shophappens.catalog.application.port.in;

public class DuplicateCatalogAttributeException extends RuntimeException {
    public DuplicateCatalogAttributeException(String code) {
        super("Attribute code already exists: " + code);
    }
}
