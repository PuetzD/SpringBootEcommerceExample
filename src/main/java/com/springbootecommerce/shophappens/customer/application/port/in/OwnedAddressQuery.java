package com.springbootecommerce.shophappens.customer.application.port.in;

import java.util.List;

public interface OwnedAddressQuery {
    List<AddressSnapshot> findForCustomer(CustomerReference customer);

    AddressSnapshot getOwned(CustomerReference customer, AddressReference address);
}
