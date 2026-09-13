package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedProductSnapshot(
        ProductVariantId variant,
        ProductReference product,
        String sku,
        String name,
        Money unitPrice,
        int quantity) {
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
