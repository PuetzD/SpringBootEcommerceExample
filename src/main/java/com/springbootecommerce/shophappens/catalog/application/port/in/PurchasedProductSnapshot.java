package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedProductSnapshot(
        ProductReference product, String sku, String name, Money unitPrice, int quantity) {
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
