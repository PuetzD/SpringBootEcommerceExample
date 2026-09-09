package com.springbootecommerce.shophappens.cart.application.port.out;

import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;

public interface GuestCartWriteGuard {
    void acquire(GuestCartId id);
}
