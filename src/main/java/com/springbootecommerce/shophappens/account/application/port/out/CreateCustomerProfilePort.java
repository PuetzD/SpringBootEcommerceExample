package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;

public interface CreateCustomerProfilePort {
    void create(AccountId accountId);
}
