package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public interface CustomerContactGateway {
    CustomerContact contact(CustomerId customerId);

    record CustomerContact(String givenName, String contactEmail) {}
}
