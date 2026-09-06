package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CheckoutItem(ProductVariantId variant, int quantity) {
    public CheckoutItem(ProductId product, int quantity) {
        this(new ProductVariantId(product.value()), quantity);
    }

    public ProductId product() {
        return new ProductId(variant.value());
    }
}
