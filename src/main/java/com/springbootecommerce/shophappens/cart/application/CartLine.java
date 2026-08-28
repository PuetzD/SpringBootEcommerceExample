package com.springbootecommerce.shophappens.cart.application;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record CartLine(
        Long productId, String sku, String name, Money unitPrice, int quantity, Money lineTotal) {}
