package com.springbootecommerce.shophappens.ordering.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Objects;

public record OrderItem(
        ProductId productId, String sku, String productName, Money unitPrice, int quantity) {
    public OrderItem {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(unitPrice);
        if (sku == null || sku.isBlank() || productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product snapshot is incomplete");
        }
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
