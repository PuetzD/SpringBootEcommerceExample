package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;

public record PurchasedProduct(
        ProductId productId, String sku, String name, Money unitPrice, int quantity) {}
