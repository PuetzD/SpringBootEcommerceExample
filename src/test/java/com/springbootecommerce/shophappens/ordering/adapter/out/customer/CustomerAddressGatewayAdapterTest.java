package com.springbootecommerce.shophappens.ordering.adapter.out.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutAddressUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.out.AvailableAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAddressGatewayAdapterTest {
    private static final CustomerId CUSTOMER_ID = new CustomerId(42L);
    private static final CustomerReference CUSTOMER = new CustomerReference(42L);
    private static final AddressReference ADDRESS = new AddressReference(11L);

    @Mock OwnedAddressQuery addresses;
    CustomerAddressGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerAddressGatewayAdapter(addresses);
    }

    @Test
    void translatesUnavailableOwnedAddressAndPreservesCause() {
        var cause = new OwnedAddressUnavailableException("Address is not owned");
        when(addresses.getOwned(CUSTOMER, ADDRESS)).thenThrow(cause);

        Throwable thrown = catchThrowable(() -> adapter.shipping(CUSTOMER_ID, ADDRESS.value()));

        assertThat(thrown).isInstanceOf(CheckoutAddressUnavailableException.class);
        assertThat(thrown.getCause()).isSameAs(cause);
    }

    @Test
    void mapsSuccessfulOwnedAddressWithoutChangingPostalFacts() {
        when(addresses.getOwned(CUSTOMER, ADDRESS)).thenReturn(snapshot());

        assertThat(adapter.shipping(CUSTOMER_ID, ADDRESS.value()))
                .isEqualTo(
                        new OrderAddress(
                                AddressRole.SHIPPING,
                                "Jane Doe",
                                "Debugging Ltd",
                                "123 Main St",
                                "Suite 4",
                                "Metropolis",
                                "NY",
                                "10001",
                                "US",
                                "+1-555-0100"));
    }

    @Test
    void mapsAvailableAddressesWithoutChangingSelectionFacts() {
        when(addresses.findForCustomer(CUSTOMER)).thenReturn(List.of(snapshot()));

        assertThat(adapter.available(CUSTOMER_ID))
                .containsExactly(
                        new AvailableAddress(
                                ADDRESS, "Jane Doe", "Metropolis", "10001", "US", true, false));
    }

    private static AddressSnapshot snapshot() {
        return new AddressSnapshot(
                CUSTOMER,
                ADDRESS,
                "Jane Doe",
                "Debugging Ltd",
                "123 Main St",
                "Suite 4",
                "Metropolis",
                "NY",
                "10001",
                "US",
                "+1-555-0100",
                true,
                false);
    }
}
