package com.springbootecommerce.shophappens.catalog.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedFacts(ProductId id, Sku sku, String name, Money price, int quantity) {}
