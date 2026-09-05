package com.springbootecommerce.shophappens.customer.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;

public interface CustomerAdministrationQuery {
    CustomerAdminPage searchCustomers(CustomerAdminSearch search);

    Optional<CustomerAdminDetail> findCustomer(CustomerId customerId);
}
