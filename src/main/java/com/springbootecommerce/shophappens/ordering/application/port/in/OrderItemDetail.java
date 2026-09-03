package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record OrderItemDetail(
        ProductId productId,
        String sku,
        String productName,
        Money unitPrice,
        int quantity,
        Money lineTotal) {}
