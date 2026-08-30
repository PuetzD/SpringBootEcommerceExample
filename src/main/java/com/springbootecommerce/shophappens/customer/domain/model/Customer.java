package com.springbootecommerce.shophappens.customer.domain.model;

import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Customer {
    private final CustomerId id;
    private final AccountId accountId;
    private final List<Address> addresses;

    private Customer(CustomerId id, AccountId accountId, List<Address> addresses) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId);
        this.addresses = new ArrayList<>(addresses);
    }

    public static Customer create(AccountId accountId) {
        return new Customer(null, accountId, new ArrayList<>());
    }

    public static Customer restore(CustomerId id, AccountId accountId, List<Address> addresses) {
        return new Customer(Objects.requireNonNull(id), accountId, addresses);
    }

    public Optional<CustomerId> id() {
        return Optional.ofNullable(id);
    }

    public AccountId accountId() {
        return accountId;
    }

    public List<Address> addresses() {
        return Collections.unmodifiableList(new ArrayList<>(addresses));
    }

    public Address addAddress(AddressDetails details, boolean shipping, boolean billing) {
        clearDefaults(shipping, billing);
        var address = Address.create(details, shipping, billing);
        addresses.add(address);
        return address;
    }

    public void updateAddress(
            AddressId id, AddressDetails details, boolean shipping, boolean billing) {
        Address owned = address(id);
        clearDefaults(shipping, billing);
        owned.update(details, shipping, billing);
    }

    public void removeAddress(AddressId id) {
        Address owned = address(id);
        addresses.remove(owned);
    }

    public Address address(AddressId id) {
        return addresses.stream()
                .filter(address -> id.equals(address.id().orElse(null)))
                .findFirst()
                .orElseThrow(
                        () ->
                                new AddressNotOwnedException(
                                        "Address " + id.value() + " is not owned"));
    }

    private void clearDefaults(boolean shipping, boolean billing) {
        if (shipping) addresses.forEach(Address::removeShippingDefault);
        if (billing) addresses.forEach(Address::removeBillingDefault);
    }
}
