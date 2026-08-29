package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;

public record CartItemSnapshot(ProductReference product, int quantity) {}
