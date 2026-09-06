package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
class CartItemKey {
    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "variant_id")
    private long variantId;

    @Override
    public boolean equals(Object o) {
        return o instanceof CartItemKey key
                && Objects.equals(cartId, key.cartId)
                && variantId == key.variantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, variantId);
    }
}
