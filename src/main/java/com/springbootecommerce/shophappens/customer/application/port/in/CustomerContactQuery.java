package com.springbootecommerce.shophappens.customer.application.port.in;

import java.util.Optional;

public interface CustomerContactQuery {
    Optional<CustomerContact> findContact(CustomerReference customer);

    record CustomerContact(String givenName, String contactEmail) {}
}
