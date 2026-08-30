package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;

public interface CustomerAddressGateway {
    OrderAddress shipping(CustomerId customerId, long addressId);

    OrderAddress billing(CustomerId customerId, long addressId);

    List<AvailableAddress> available(CustomerId customerId);
}
