package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.List;

public record ProductAdminView(
        ProductReference product,
        String sku,
        String name,
        String description,
        Money price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        ProductRevision revision,
        List<ProductCategorySummary> categories) {
    public ProductAdminView {
        categories = List.copyOf(categories == null ? List.of() : categories);
    }
}
