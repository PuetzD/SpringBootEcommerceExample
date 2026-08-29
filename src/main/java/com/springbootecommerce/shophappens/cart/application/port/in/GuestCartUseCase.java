package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;

public interface GuestCartUseCase {
    void changeQuantity(GuestCartReference guest, ProductReference product, int quantity);

    void remove(GuestCartReference guest, ProductReference product);

    GuestCartSnapshot getSnapshot(GuestCartReference guest);
}
