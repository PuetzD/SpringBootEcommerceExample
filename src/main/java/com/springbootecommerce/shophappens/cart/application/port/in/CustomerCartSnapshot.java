package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import java.util.List;

public record CustomerCartSnapshot(CustomerReference customer, List<CartItemSnapshot> items) {
    public CustomerCartSnapshot {
        items = List.copyOf(items);
    }

    public boolean empty() {
        return items.isEmpty();
    }
}
