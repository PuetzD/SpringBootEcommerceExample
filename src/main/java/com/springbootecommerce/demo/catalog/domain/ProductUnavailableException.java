package com.springbootecommerce.demo.catalog.domain;

public final class ProductUnavailableException extends RuntimeException {
    private final Long productId;
    private final String sku;

    public ProductUnavailableException(Long productId, String sku) {
        super("Product is unavailable");
        this.productId = productId;
        this.sku = sku;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }
}
