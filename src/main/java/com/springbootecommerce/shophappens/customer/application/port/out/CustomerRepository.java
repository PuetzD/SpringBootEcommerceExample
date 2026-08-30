package com.springbootecommerce.shophappens.customer.application.port.out;

import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findById(CustomerId id);

    Optional<Customer> findByAccountId(AccountId accountId);

    Customer save(Customer customer);
}
