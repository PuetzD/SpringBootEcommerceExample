package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record ProductVariantAdminView(
        ProductVariantId variant,
        ProductReference product,
        String sku,
        Money price,
        int stockQuantity,
        String imageUrl,
        boolean active,
        boolean defaultVariant,
        ProductRevision productRevision) {}
