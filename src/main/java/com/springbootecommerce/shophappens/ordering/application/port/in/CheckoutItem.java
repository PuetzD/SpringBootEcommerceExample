package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Objects;

public record CheckoutItem(
        ProductVariantId variant,
        ProductId product,
        String sku,
        String productName,
        Money unitPrice,
        int quantity) {
    public CheckoutItem {
        Objects.requireNonNull(variant, "variant");
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(productName, "productName");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
