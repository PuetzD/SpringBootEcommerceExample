package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public interface GuestCartUseCase {
    void changeQuantity(GuestCartReference guest, ProductId product, int quantity);

    void remove(GuestCartReference guest, ProductId product);

    GuestCartSnapshot getSnapshot(GuestCartReference guest);
}
