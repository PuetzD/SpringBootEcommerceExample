package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public interface GuestCartUseCase {
    void changeQuantity(GuestCartReference guest, ProductVariantId variant, int quantity);

    void remove(GuestCartReference guest, ProductVariantId variant);

    GuestCartSnapshot getSnapshot(GuestCartReference guest);
}
