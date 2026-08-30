package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;

public record CheckoutItem(ProductReference product, int quantity) {}
