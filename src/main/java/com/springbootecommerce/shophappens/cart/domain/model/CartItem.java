package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public record CartItem(ProductVariantId variantId, Quantity quantity) {}
