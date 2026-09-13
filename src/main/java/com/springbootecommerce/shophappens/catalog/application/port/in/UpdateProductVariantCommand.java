package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record UpdateProductVariantCommand(
        String sku, Money price, int stockQuantity, String imageUrl, boolean active) {}
