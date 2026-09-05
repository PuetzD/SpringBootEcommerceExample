package com.springbootecommerce.shophappens.catalog.application.port.in;

public final class PublishedInsufficientStockException extends RuntimeException {
    private final ProductReference product;
    private final String sku;
    private final int requestedQuantity;
    private final int availableQuantity;

    public PublishedInsufficientStockException(
            ProductReference product, String sku, int requestedQuantity, int availableQuantity) {
        this.product = product;
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public ProductReference product() {
        return product;
    }

    public String sku() {
        return sku;
    }

    public int requestedQuantity() {
        return requestedQuantity;
    }

    public int availableQuantity() {
        return availableQuantity;
    }
}
