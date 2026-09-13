package com.springbootecommerce.shophappens.ordering.adapter.out.customer;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerContactQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerContactGateway;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerContactGatewayAdapter implements CustomerContactGateway {
    private final CustomerContactQuery contacts;

    @Override
    public CustomerContact contact(CustomerId customerId) {
        return contacts.findContact(new CustomerReference(customerId.value()))
                .map(contact -> new CustomerContact(contact.givenName(), contact.contactEmail()))
                .orElseThrow(() -> new IllegalStateException("Customer contact is unavailable"));
    }
}
