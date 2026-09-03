package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public record CartItemSnapshot(ProductId product, int quantity) {}
