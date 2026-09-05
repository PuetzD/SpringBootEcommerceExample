package com.springbootecommerce.shophappens.catalog.domain.exception;

import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public class ProductUnavailableException extends RuntimeException {
    private final ProductId productId;
    private final Sku sku;

    public ProductUnavailableException(ProductId productId, Sku sku) {
        this.productId = productId;
        this.sku = sku;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Sku getSku() {
        return sku;
    }
}
