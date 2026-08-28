package com.springbootecommerce.shophappens.cart.application;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.List;

public record CartDetails(List<CartLine> lines, Money total) {

    public CartDetails {
        lines = List.copyOf(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
