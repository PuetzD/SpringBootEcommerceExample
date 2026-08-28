package com.springbootecommerce.shophappens.catalog.application;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record ProductSummary(
        Long id,
        String sku,
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl) {
    public boolean inStock() {
        return stockQuantity > 0;
    }
}
