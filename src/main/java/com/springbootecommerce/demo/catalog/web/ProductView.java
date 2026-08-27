package com.springbootecommerce.demo.catalog.web;

import java.math.BigDecimal;

public record ProductView(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        boolean active) {}
