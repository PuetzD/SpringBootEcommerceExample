package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;

public interface CreateCustomerProfilePort {
    CustomerReference create(AccountId accountId);
}
