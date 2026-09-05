package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface MergeGuestCartUseCase {
    void merge(GuestCartReference guest, CustomerId customer);
}
