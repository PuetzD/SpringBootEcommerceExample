package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.List;

public record CheckoutPreparation(
        CustomerId customer,
        List<CheckoutItem> items,
        List<CheckoutAddress> addresses,
        List<ProductVariantId> unavailableVariants) {
    public CheckoutPreparation {
        items = List.copyOf(items);
        addresses = List.copyOf(addresses);
        unavailableVariants = List.copyOf(unavailableVariants);
    }

    public Money merchandiseTotal() {
        return items.stream().map(CheckoutItem::lineTotal).reduce(Money.zero(), Money::add);
    }
}
