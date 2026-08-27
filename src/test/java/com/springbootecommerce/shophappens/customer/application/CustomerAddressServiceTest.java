package com.springbootecommerce.shophappens.customer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.domain.Address;
import com.springbootecommerce.shophappens.customer.persistence.AddressRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {

    @Mock private AddressRepository repository;

    @Test
    void returnsOnlyAddressesOwnedByCustomer() {
        var service = new CustomerAddressService(repository);
        var address = address(11L, 42L, "Daniela Pütz", "Marburg");
        when(repository.findByCustomerIdOrderByDefaultShippingDesc(42L))
                .thenReturn(List.of(address));

        assertThat(service.findForCustomer(42L))
                .extracting(AddressSnapshot::recipientName)
                .containsExactly("Daniela Pütz");
    }

    @Test
    void rejectsMissingOrForeignAddress() {
        var service = new CustomerAddressService(repository);
        when(repository.findByIdAndCustomerId(11L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwned(42L, 11L))
                .isInstanceOf(AddressNotOwnedException.class);
    }

    private Address address(long id, long customerId, String recipientName, String city) {
        var address = new Address();
        address.setRecipientName(recipientName);
        address.setAddressLine1("1 Main Street");
        address.setCity(city);
        address.setPostalCode("35037");
        address.setCountryCode("DE");
        return address;
    }
}
