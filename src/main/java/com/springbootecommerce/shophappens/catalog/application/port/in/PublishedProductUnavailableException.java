package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public final class PublishedProductUnavailableException extends ProductUnavailableException {
    public PublishedProductUnavailableException(ProductId productId, Sku sku) {
        super(productId, sku);
    }
}
