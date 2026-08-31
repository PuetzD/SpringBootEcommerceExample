package com.springbootecommerce.shophappens.cart.domain.exception;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public final class CartItemNotFoundException extends RuntimeException {
    private final ProductId productId;

    public CartItemNotFoundException(ProductId productId) {
        this.productId = productId;
    }

    public ProductId getProductId() {
        return productId;
    }
}
