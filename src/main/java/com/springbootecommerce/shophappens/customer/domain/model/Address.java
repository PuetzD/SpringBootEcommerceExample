package com.springbootecommerce.shophappens.customer.domain.model;

import java.util.Objects;
import java.util.Optional;

public final class Address {
    private final AddressId id;
    private AddressDetails details;
    private boolean defaultShipping;
    private boolean defaultBilling;

    private Address(
            AddressId id, AddressDetails details, boolean defaultShipping, boolean defaultBilling) {
        this.id = id;
        this.details = Objects.requireNonNull(details);
        this.defaultShipping = defaultShipping;
        this.defaultBilling = defaultBilling;
    }

    static Address create(AddressDetails details, boolean shipping, boolean billing) {
        return new Address(null, details, shipping, billing);
    }

    public static Address restore(
            AddressId id, AddressDetails details, boolean shipping, boolean billing) {
        return new Address(Objects.requireNonNull(id), details, shipping, billing);
    }

    void update(AddressDetails replacement, boolean shipping, boolean billing) {
        details = Objects.requireNonNull(replacement);
        defaultShipping = shipping;
        defaultBilling = billing;
    }

    void removeShippingDefault() {
        defaultShipping = false;
    }

    void removeBillingDefault() {
        defaultBilling = false;
    }

    public Optional<AddressId> id() {
        return Optional.ofNullable(id);
    }

    public AddressDetails details() {
        return details;
    }

    public boolean defaultShipping() {
        return defaultShipping;
    }

    public boolean defaultBilling() {
        return defaultBilling;
    }
}
