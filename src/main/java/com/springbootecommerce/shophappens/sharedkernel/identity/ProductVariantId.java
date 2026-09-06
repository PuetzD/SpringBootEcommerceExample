package com.springbootecommerce.shophappens.sharedkernel.identity;

public record ProductVariantId(long value) {
    public ProductVariantId {
        if (value < 1) throw new IllegalArgumentException("Product variant ID must be positive");
    }
}
