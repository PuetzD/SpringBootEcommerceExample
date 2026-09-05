package com.springbootecommerce.shophappens.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomerTest {
    private static final AddressDetails Testcity =
            new AddressDetails(
                    "Bard the Magnificent Debugger",
                    null,
                    "1 Main Street",
                    null,
                    "Greymoor",
                    null,
                    "35037",
                    "DE",
                    null);

    @Test
    void createsCustomerForAnAccountAndOwnsItsAddresses() {
        var customer =
                Customer.create(
                        new AccountId(42L),
                        " Ada ",
                        " Lovelace ",
                        new ContactEmail(" Ada@Example.COM "));

        Address first = customer.addAddress(Testcity, true, true);
        Address second =
                customer.addAddress(
                        new AddressDetails(
                                "Bard the Magnificent Debugger",
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
        assertThat(customer.givenName()).isEqualTo("Ada");
        assertThat(customer.familyName()).isEqualTo("Lovelace");
        assertThat(customer.contactEmail()).isEqualTo(new ContactEmail("ada@example.com"));
        assertThat(customer.addresses()).hasSize(2);
        assertThat(first.defaultShipping()).isFalse();
        assertThat(second.defaultShipping()).isTrue();
        assertThat(first.defaultBilling()).isTrue();
    }

    @Test
    void requiresNonBlankTrimmedNames() {
        assertThatThrownBy(
                        () ->
                                Customer.create(
                                        new AccountId(42L),
                                        " ",
                                        "Lovelace",
                                        new ContactEmail("ada@example.com")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () ->
                                Customer.create(
                                        new AccountId(42L),
                                        "Ada",
                                        null,
                                        new ContactEmail("ada@example.com")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restoresIdentityAndDoesNotExposeMutableAddressCollections() {
        var address = Address.restore(new AddressId(11L), Testcity, true, true);
        var customer =
                Customer.restore(
                        new CustomerId(7L),
                        new AccountId(42L),
                        "Ada",
                        "Lovelace",
                        new ContactEmail(" ADA@EXAMPLE.COM "),
                        List.of(address));

        var addresses = customer.addresses();
        assertThatThrownBy(addresses::clear).isInstanceOf(UnsupportedOperationException.class);

        assertThat(customer.givenName()).isEqualTo("Ada");
        assertThat(customer.familyName()).isEqualTo("Lovelace");
        assertThat(customer.contactEmail()).isEqualTo(new ContactEmail("ada@example.com"));
        assertThat(customer.addresses()).containsExactly(address);
    }

    @Test
    void rejectsForeignAddressAndInvalidPostalDetails() {
        var customer =
                Customer.create(
                        new AccountId(42L), "Ada", "Lovelace", new ContactEmail("ada@example.com"));

        assertThatThrownBy(() -> customer.address(new AddressId(999L)))
                .isInstanceOf(AddressNotOwnedException.class);
        assertThatThrownBy(() -> new AddressDetails("", null, "", null, "", null, "", "DEU", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removesAnOwnedAddressAndRejectsForeignRemoval() {
        var address = Address.restore(new AddressId(11L), Testcity, true, true);
        var customer =
                Customer.restore(
                        new CustomerId(7L),
                        new AccountId(42L),
                        "Ada",
                        "Lovelace",
                        new ContactEmail("ada@example.com"),
                        List.of(address));

        customer.removeAddress(new AddressId(11L));

        assertThat(customer.addresses()).isEmpty();
        assertThatThrownBy(() -> customer.removeAddress(new AddressId(999L)))
                .isInstanceOf(AddressNotOwnedException.class);
    }
}
