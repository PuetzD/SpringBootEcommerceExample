package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.account.domain.model.AccountId;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public interface CreateCustomerProfilePort {
    CustomerReference create(AccountId accountId);
}
