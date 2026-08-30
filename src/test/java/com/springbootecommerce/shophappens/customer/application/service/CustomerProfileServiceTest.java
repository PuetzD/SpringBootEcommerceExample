package com.springbootecommerce.shophappens.customer.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.AddressId;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {
    @Mock CustomerRepository customers;
    @InjectMocks CustomerProfileService service;

    @Test
    void createsOneProfileForAnExternalAccount() {
        when(customers.findByAccountId(new AccountId(42L))).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class)))
                .thenAnswer(
                        invocation ->
                                Customer.restore(
                                        new CustomerId(7L), new AccountId(42L), List.of()));

        assertThat(service.create(new ExternalAccountId(42L))).isEqualTo(new CustomerReference(7L));
    }

    @Test
    void returnsOnlyAnOwnedImmutableAddressSnapshot() {
        var customer = restoredCustomerWithAddress(7L, 11L);
        when(customers.findById(new CustomerId(7L))).thenReturn(Optional.of(customer));

        AddressSnapshot result =
                service.getOwned(new CustomerReference(7L), new AddressReference(11L));

        assertThat(result.recipientName()).isEqualTo("Bard the Magnificent Debugger");
        assertThat(result.customer()).isEqualTo(new CustomerReference(7L));
    }

    @Test
    void removesAnOwnedAddress() {
        var customer = restoredCustomerWithAddress(7L, 11L);
        when(customers.findById(new CustomerId(7L))).thenReturn(Optional.of(customer));
        when(customers.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.remove(new CustomerReference(7L), new AddressReference(11L));

        assertThat(customer.addresses()).isEmpty();
    }

    private Customer restoredCustomerWithAddress(long customerId, long addressId) {
        var address =
                Address.restore(
                        new AddressId(addressId),
                        new AddressDetails(
                                "Bard the Magnificent Debugger",
                                null,
                                "1 Main Street",
                                null,
                                "Greymoor",
                                null,
                                "35037",
                                "DE",
                                null),
                        true,
                        true);
        return Customer.restore(new CustomerId(customerId), new AccountId(42L), List.of(address));
    }
}
