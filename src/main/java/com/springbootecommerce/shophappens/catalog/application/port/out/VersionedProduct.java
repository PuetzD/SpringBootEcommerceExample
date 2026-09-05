package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.Product;

public record VersionedProduct(Product product, long revision) {
    public VersionedProduct {
        if (product == null) throw new NullPointerException("product");
        if (revision < 0)
            throw new IllegalArgumentException("Product revision must not be negative");
    }
}
