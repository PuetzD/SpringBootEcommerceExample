package com.springbootecommerce.shophappens.ordering.application.port.in;

public record CheckoutItem(ProductReference product, int quantity) {}
