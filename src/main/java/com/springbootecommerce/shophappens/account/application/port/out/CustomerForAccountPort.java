package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;

public interface CustomerForAccountPort {
    Optional<CustomerId> find(AccountId accountId);
}
