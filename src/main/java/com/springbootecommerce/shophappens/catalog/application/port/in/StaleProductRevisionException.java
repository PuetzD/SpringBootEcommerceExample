package com.springbootecommerce.shophappens.catalog.application.port.in;

public class StaleProductRevisionException extends RuntimeException {
    private final ProductReference product;
    private final ProductRevision expectedRevision;

    public StaleProductRevisionException(
            ProductReference product, ProductRevision expectedRevision) {
        super("Product revision is stale: " + product.value());
        this.product = product;
        this.expectedRevision = expectedRevision;
    }

    public ProductReference product() {
        return product;
    }

    public ProductRevision expectedRevision() {
        return expectedRevision;
    }
}
