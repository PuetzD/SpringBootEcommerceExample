package com.springbootecommerce.shophappens.customer.application.port.in;

import java.util.Optional;

/**
 * Published inbound port for resolving the current customer from authentication context. Used by
 * all web adapters to access the current customer reference.
 */
public interface CurrentCustomerIdentity {
    Optional<CustomerReference> current();
}
