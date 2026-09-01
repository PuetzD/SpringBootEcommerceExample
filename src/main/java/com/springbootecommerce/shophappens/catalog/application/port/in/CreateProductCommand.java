package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

import java.util.Set;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl,
        Set<CategoryReference> categories) {
    public CreateProductCommand {
        categories = Set.copyOf(categories == null ? Set.of() : categories);
    }
}
