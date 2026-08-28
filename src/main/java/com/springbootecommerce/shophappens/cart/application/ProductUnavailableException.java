package com.springbootecommerce.shophappens.cart.application;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(Long productId) {
        super("Product " + productId + " is not available");
    }
}
