package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;

public interface RemoveCustomerProfilePort {
    void remove(AccountId accountId);
}
