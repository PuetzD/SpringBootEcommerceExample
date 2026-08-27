package com.springbootecommerce.shophappens.customer.application;

public final class AddressNotOwnedException extends RuntimeException {
    private final Long customerId;
    private final Long addressId;

    public AddressNotOwnedException(Long customerId, Long addressId) {
        super("Address is not owned by customer");
        this.customerId = customerId;
        this.addressId = addressId;
    }

    public Long customerId() {
        return customerId;
    }

    public Long addressId() {
        return addressId;
    }
}
