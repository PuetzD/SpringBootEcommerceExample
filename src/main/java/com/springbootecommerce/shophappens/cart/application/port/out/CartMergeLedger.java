package com.springbootecommerce.shophappens.cart.application.port.out;

import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;

public interface CartMergeLedger {
    boolean claim(GuestCartId guestCartId, CustomerId customerId);
}
