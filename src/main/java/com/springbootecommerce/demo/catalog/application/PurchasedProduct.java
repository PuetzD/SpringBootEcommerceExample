package com.springbootecommerce.demo.catalog.application;

import com.springbootecommerce.demo.sharedkernel.money.Money;

public record PurchasedProduct(
        Long productId, String sku, String name, Money unitPrice, int quantity) {}
