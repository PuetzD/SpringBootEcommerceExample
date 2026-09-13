package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CartItemSnapshot(ProductVariantId variant, int quantity) {}
