package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedProduct(
        ProductVariantId variantId,
        ProductId productId,
        String sku,
        String name,
        Money unitPrice,
        int quantity) {}
