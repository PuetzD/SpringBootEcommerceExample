package com.springbootecommerce.shophappens.customer.application.port.in;

import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;

public final class OwnedAddressUnavailableException extends AddressNotOwnedException {
    public OwnedAddressUnavailableException(String message) {
        super(message);
    }
}
