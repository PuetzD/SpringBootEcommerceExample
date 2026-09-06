package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.Objects;

public record PurchaseLine(ProductVariantId variant, int quantity) {
    public PurchaseLine(ProductReference product, int quantity) {
        this(new ProductVariantId(product.value()), quantity);
    }

    public PurchaseLine {
        Objects.requireNonNull(variant);
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
    }
}
