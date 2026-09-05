package com.springbootecommerce.shophappens.catalog.application.port.in;

public class ProductNotFoundException extends RuntimeException {
    private final ProductReference product;

    public ProductNotFoundException(ProductReference product) {
        super("Product not found: " + product.value());
        this.product = product;
    }

    public ProductReference product() {
        return product;
    }
}
