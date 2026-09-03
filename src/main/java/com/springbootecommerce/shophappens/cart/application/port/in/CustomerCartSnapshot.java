package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;

public record CustomerCartSnapshot(CustomerId customer, List<CartItemSnapshot> items) {
    public CustomerCartSnapshot {
        items = List.copyOf(items);
    }

    public boolean empty() {
        return items.isEmpty();
    }
}
