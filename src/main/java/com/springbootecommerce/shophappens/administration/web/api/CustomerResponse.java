package com.springbootecommerce.shophappens.administration.web.api;

import java.time.Instant;
import java.util.List;

public record CustomerResponse(
        long id,
        String givenName,
        String familyName,
        String contactEmail,
        Instant createdAt,
        Long accountId,
        List<CustomerAddressResponse> addresses,
        List<CustomerOrderResponse> orders) {
    public CustomerResponse {
        addresses = List.copyOf(addresses == null ? List.of() : addresses);
        orders = List.copyOf(orders == null ? List.of() : orders);
    }
}
