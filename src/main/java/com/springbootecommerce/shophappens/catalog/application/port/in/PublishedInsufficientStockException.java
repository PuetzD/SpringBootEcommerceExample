package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public final class PublishedInsufficientStockException extends InsufficientStockException {
    public PublishedInsufficientStockException(
            ProductId productId, Sku sku, int requestedQuantity, int availableQuantity) {
        super(productId, sku, requestedQuantity, availableQuantity);
    }
}
