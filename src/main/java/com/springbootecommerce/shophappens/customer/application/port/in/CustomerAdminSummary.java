package com.springbootecommerce.shophappens.customer.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public record CustomerAdminSummary(
        CustomerId customerId, String givenName, String familyName, String contactEmail) {}
