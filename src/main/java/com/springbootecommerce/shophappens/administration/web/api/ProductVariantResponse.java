package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;

public record ProductVariantResponse(
        long id,
        long productId,
        String sku,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        boolean defaultVariant,
        long productRevision) {}
