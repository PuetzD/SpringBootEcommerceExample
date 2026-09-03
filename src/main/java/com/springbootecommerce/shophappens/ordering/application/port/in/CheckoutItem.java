package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public record CheckoutItem(ProductId product, int quantity) {}
