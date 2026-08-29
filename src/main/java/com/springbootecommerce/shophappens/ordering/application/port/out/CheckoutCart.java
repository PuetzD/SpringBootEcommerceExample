package com.springbootecommerce.shophappens.ordering.application.port.out;

import java.util.List;

public record CheckoutCart(List<RequestedProduct> products) {
    public CheckoutCart {
        products = List.copyOf(products);
    }

    public boolean empty() {
        return products.isEmpty();
    }
}
