package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
class OrderAddressKey implements Serializable {
    @Column(name = "order_id")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_role")
    private AddressRole addressRole;

    protected OrderAddressKey() {}

    OrderAddressKey(UUID orderId, AddressRole addressRole) {
        this.orderId = orderId;
        this.addressRole = addressRole;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OrderAddressKey key
                && Objects.equals(orderId, key.orderId)
                && addressRole == key.addressRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, addressRole);
    }
}
