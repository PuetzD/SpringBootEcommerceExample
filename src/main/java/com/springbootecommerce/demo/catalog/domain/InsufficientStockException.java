package com.springbootecommerce.demo.catalog.domain;

public final class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final String sku;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(
            Long productId, String sku, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock");
        this.productId = productId;
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
