package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public record CartItem(ProductId productId, Quantity quantity) {}
