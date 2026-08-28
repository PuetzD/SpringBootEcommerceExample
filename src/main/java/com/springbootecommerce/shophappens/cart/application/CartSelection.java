package com.springbootecommerce.shophappens.cart.application;

import java.util.List;

public record CartSelection(Long customerId, List<CartSelectionItem> items) {

    public CartSelection {
        items = List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
