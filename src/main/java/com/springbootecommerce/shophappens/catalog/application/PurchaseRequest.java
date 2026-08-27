package com.springbootecommerce.shophappens.catalog.application;

public record PurchaseRequest(Long productId, int quantity) {

    public PurchaseRequest {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
    }
}
