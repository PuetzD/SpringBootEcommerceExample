package com.springbootecommerce.shophappens.catalog.application.port.in;

public class DuplicateSkuException extends RuntimeException {
    private final String sku;

    public DuplicateSkuException(String sku) {
        super("Product SKU already exists: " + sku);
        this.sku = sku;
    }

    public String sku() {
        return sku;
    }
}
