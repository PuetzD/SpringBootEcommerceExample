package com.springbootecommerce.shophappens.catalog.application.port.in;

public final class PublishedProductUnavailableException extends RuntimeException {
    private final ProductReference product;
    private final String sku;

    public PublishedProductUnavailableException(ProductReference product, String sku) {
        this.product = product;
        this.sku = sku;
    }

    public ProductReference product() {
        return product;
    }

    public String sku() {
        return sku;
    }
}
