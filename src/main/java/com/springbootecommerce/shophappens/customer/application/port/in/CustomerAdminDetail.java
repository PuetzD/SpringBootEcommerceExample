package com.springbootecommerce.shophappens.customer.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;

public record CustomerAdminDetail(
        CustomerId customerId,
        AccountId accountId,
        String givenName,
        String familyName,
        String contactEmail,
        List<CustomerAdminAddressView> addresses) {
    public CustomerAdminDetail {
        addresses = List.copyOf(addresses == null ? List.of() : addresses);
    }
}
