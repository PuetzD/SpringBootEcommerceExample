package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Set;

public record UpdateProductCommand(
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        Set<CategoryReference> categories) {
    public UpdateProductCommand {
        categories = Set.copyOf(categories == null ? Set.of() : categories);
    }
}
