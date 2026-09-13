package com.springbootecommerce.shophappens.cart.domain.exception;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public final class CartItemNotFoundException extends RuntimeException {
    private final ProductVariantId variantId;

    public CartItemNotFoundException(ProductVariantId variantId) {
        this.variantId = variantId;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }
}
