package com.springbootecommerce.shophappens.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;
import org.junit.jupiter.api.Test;

class CustomerTest {
    private static final AddressDetails Testcity =
            new AddressDetails(
                    "Alex Example",
                    null,
                    "1 Main Street",
                    null,
                    "Testcity",
                    null,
                    "35037",
                    "DE",
                    null);

    @Test
    void createsCustomerForAnAccountAndOwnsItsAddresses() {
        var customer = Customer.create(new AccountId(42L));

        Address first = customer.addAddress(Testcity, true, true);
        Address second =
                customer.addAddress(
                        new AddressDetails(
                                "Alex Example",
                                null,
                                "2 Side Street",
                                null,
                                "Berlin",
                                null,
                                "10115",
                                "DE",
                                null),
                        true,
                        false);

        assertThat(customer.accountId()).isEqualTo(new AccountId(42L));
        assertThat(customer.addresses()).hasSize(2);
        assertThat(first.defaultShipping()).isFalse();
        assertThat(second.defaultShipping()).isTrue();
        assertThat(first.defaultBilling()).isTrue();
    }

    @Test
    void rejectsForeignAddressAndInvalidPostalDetails() {
        var customer = Customer.create(new AccountId(42L));

        assertThatThrownBy(() -> customer.address(new AddressId(999L)))
                .isInstanceOf(AddressNotOwnedException.class);
        assertThatThrownBy(() -> new AddressDetails("", null, "", null, "", null, "", "DEU", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
