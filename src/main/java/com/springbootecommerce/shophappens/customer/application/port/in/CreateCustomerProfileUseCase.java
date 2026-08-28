package com.springbootecommerce.shophappens.customer.application.port.in;

public interface CreateCustomerProfileUseCase {
    CustomerReference create(ExternalAccountId accountId);
}
