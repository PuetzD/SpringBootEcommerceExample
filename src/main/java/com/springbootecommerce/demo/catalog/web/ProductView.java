package com.springbootecommerce.demo.catalog.web;

import com.springbootecommerce.demo.sharedkernel.money.Money;

public record ProductView(
        Long id,
        String sku,
        String name,
        String description,
        Money price,
        Integer stockQuantity,
        String imageUrl,
        boolean active) {}
