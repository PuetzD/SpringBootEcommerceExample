package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface RemoveCustomerCartPort {
    void remove(CustomerId customerId);
}
