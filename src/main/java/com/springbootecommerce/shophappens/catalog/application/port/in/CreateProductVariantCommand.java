package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record CreateProductVariantCommand(
        String sku, Money price, int stockQuantity, String imageUrl, boolean active) {}
