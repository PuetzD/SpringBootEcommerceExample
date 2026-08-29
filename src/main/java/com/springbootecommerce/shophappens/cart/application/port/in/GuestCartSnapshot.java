package com.springbootecommerce.shophappens.cart.application.port.in;

import java.util.List;

public record GuestCartSnapshot(GuestCartReference guest, List<CartItemSnapshot> items) {
    public GuestCartSnapshot {
        items = List.copyOf(items);
    }

    public boolean empty() {
        return items.isEmpty();
    }
}
