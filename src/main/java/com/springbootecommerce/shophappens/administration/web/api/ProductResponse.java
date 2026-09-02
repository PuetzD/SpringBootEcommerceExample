package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        List<CategorySummaryResponse> categories,
        long revision,
        String self,
        String edit,
        String delete) {}
