package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface CheckoutLock {
    void acquire(CustomerId customerId, CheckoutId checkoutId);
}
