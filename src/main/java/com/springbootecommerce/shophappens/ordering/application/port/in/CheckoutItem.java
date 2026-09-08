package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CheckoutItem(ProductVariantId variant, int quantity) {}
