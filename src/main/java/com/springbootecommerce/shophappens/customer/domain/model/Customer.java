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
    private final String givenName;
    private final String familyName;
    private final ContactEmail contactEmail;
    private final List<Address> addresses;

    private Customer(
            CustomerId id,
            AccountId accountId,
            String givenName,
            String familyName,
            ContactEmail contactEmail,
            List<Address> addresses) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId);
        this.givenName = requiredName(givenName, "Given name");
        this.familyName = requiredName(familyName, "Family name");
        this.contactEmail = Objects.requireNonNull(contactEmail);
        this.addresses = new ArrayList<>(Objects.requireNonNull(addresses));
    }

    public static Customer create(
            AccountId accountId, String givenName, String familyName, ContactEmail contactEmail) {
        return new Customer(
                null, accountId, givenName, familyName, contactEmail, new ArrayList<>());
    }

    public static Customer restore(
            CustomerId id,
            AccountId accountId,
            String givenName,
            String familyName,
            ContactEmail contactEmail,
            List<Address> addresses) {
        return new Customer(
                Objects.requireNonNull(id),
                accountId,
                givenName,
                familyName,
                contactEmail,
                addresses);
    }

    public Optional<CustomerId> id() {
        return Optional.ofNullable(id);
    }

    public AccountId accountId() {
        return accountId;
    }

    public String givenName() {
        return givenName;
    }

    public String familyName() {
        return familyName;
    }

    public ContactEmail contactEmail() {
        return contactEmail;
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

    private static String requiredName(String name, String label) {
        if (name == null) throw new IllegalArgumentException(label + " must not be null");
        String normalized = name.strip();
        if (normalized.isBlank()) throw new IllegalArgumentException(label + " is required");
        return normalized;
    }
}
