package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CartItem(ProductVariantId variantId, Quantity quantity) {
    public CartItem(ProductId productId, Quantity quantity) {
        this(new ProductVariantId(productId.value()), quantity);
    }

    public ProductId productId() {
        return new ProductId(variantId.value());
    }
}
