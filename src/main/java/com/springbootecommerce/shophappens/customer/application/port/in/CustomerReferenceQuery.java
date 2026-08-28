package com.springbootecommerce.shophappens.customer.application.port.in;

import java.util.Optional;

public interface CustomerReferenceQuery {
    Optional<CustomerReference> findByExternalAccountId(ExternalAccountId accountId);
}
