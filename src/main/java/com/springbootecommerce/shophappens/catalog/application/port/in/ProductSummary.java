package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record ProductSummary(
        ProductReference product,
        String sku,
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl) {}
