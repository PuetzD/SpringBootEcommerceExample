package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.util.Collections;
import java.util.List;

public record CheckoutPreparation(
        CustomerReference customer,
        List<CheckoutItem> items,
        List<CheckoutAddress> addresses) {
    public CheckoutPreparation {
        items = List.copyOf(items);
        addresses = List.copyOf(addresses);
    }
}
