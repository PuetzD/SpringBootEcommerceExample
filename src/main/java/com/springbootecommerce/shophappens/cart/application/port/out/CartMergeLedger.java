package com.springbootecommerce.shophappens.cart.application.port.out;

import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface CartMergeLedger {
    boolean claim(GuestCartId guestCartId, CustomerId customerId);
}
