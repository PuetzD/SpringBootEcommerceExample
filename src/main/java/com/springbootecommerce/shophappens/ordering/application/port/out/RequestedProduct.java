package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record RequestedProduct(ProductVariantId variantId, int quantity) {
    public RequestedProduct(ProductId productId, int quantity) {
        this(new ProductVariantId(productId.value()), quantity);
    }

    public ProductId productId() {
        return new ProductId(variantId.value());
    }
}
