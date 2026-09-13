package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record RequestedProduct(ProductVariantId variantId, int quantity) {}
