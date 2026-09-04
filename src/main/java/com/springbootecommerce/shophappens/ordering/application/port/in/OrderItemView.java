package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record OrderItemView(
        long productId,
        String sku,
        String productName,
        Money unitPrice,
        int quantity,
        Money lineTotal) {}
