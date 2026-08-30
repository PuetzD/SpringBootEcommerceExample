package com.springbootecommerce.shophappens.catalog.application.command;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record UpdateProductCommand(
        String sku,
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl,
        boolean active) {}
