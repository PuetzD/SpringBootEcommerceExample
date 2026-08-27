package com.springbootecommerce.shophappens.catalog.application;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedProduct(
        Long productId, String sku, String name, Money unitPrice, int quantity) {}
