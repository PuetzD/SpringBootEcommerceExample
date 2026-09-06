package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CartItemSnapshot(ProductVariantId variant, int quantity) {
    public CartItemSnapshot(ProductId product, int quantity) {
        this(new ProductVariantId(product.value()), quantity);
    }

    public ProductId product() {
        return new ProductId(variant.value());
    }
}
