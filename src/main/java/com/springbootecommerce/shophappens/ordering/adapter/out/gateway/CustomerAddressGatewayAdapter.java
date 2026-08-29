package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressQuery;
import com.springbootecommerce.shophappens.ordering.application.port.out.AvailableAddress;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerAddressGateway;
import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class CustomerAddressGatewayAdapter implements CustomerAddressGateway {
    private final OwnedAddressQuery addresses;

    CustomerAddressGatewayAdapter(OwnedAddressQuery addresses) {
        this.addresses = addresses;
    }

    @Override
    public OrderAddress shipping(CustomerId customerId, long addressId) {
        return toOrderAddress(
                addresses.getOwned(toCustomer(customerId), new AddressReference(addressId)));
    }

    @Override
    public OrderAddress billing(CustomerId customerId, long addressId) {
        return toOrderAddress(
                addresses.getOwned(toCustomer(customerId), new AddressReference(addressId)));
    }

    @Override
    public List<AvailableAddress> available(CustomerId customerId) {
        return addresses.findForCustomer(toCustomer(customerId)).stream()
                .map(
                        a ->
                                new AvailableAddress(
                                        a.address(),
                                        a.recipientName(),
                                        a.city(),
                                        a.postalCode(),
                                        a.countryCode(),
                                        a.defaultShipping(),
                                        a.defaultBilling()))
                .toList();
    }

    private CustomerReference toCustomer(CustomerId customerId) {
        return new CustomerReference(customerId.value());
    }

    private OrderAddress toOrderAddress(AddressSnapshot snapshot) {
        return new OrderAddress(
                AddressRole.SHIPPING,
                snapshot.recipientName(),
                snapshot.companyName(),
                snapshot.addressLine1(),
                snapshot.addressLine2(),
                snapshot.city(),
                snapshot.region(),
                snapshot.postalCode(),
                snapshot.countryCode(),
                snapshot.phoneNumber());
    }
}
