package com.springbootecommerce.shophappens.administration.application.port.in;

import java.math.BigDecimal;
import java.util.List;

public record ProductAdminView(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        List<ProductCategorySummary> categories) {}
