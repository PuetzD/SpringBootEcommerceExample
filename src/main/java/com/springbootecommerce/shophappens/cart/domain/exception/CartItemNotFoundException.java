package com.springbootecommerce.shophappens.cart.domain.exception;

import com.springbootecommerce.shophappens.cart.domain.model.ProductId;

public final class CartItemNotFoundException extends RuntimeException {
    private final ProductId productId;

    public CartItemNotFoundException(ProductId productId) {
        super("Cart item not found for product " + productId);
        this.productId = productId;
    }

    public ProductId getProductId() {
        return productId;
    }
}
