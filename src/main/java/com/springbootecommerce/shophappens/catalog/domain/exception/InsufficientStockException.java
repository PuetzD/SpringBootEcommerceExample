package com.springbootecommerce.shophappens.catalog.domain.exception;

import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public final class InsufficientStockException extends RuntimeException {
    private final ProductId productId;
    private final Sku sku;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(
            ProductId productId, Sku sku, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock");
        this.productId = productId;
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Sku getSku() {
        return sku;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
