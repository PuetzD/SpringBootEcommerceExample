package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_cart_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class CustomerCartItemJpaEntity {
    @EmbeddedId private CartItemKey key;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cartId")
    @JoinColumn(name = "cart_id")
    private CustomerCartJpaEntity cart;

    static CustomerCartItemJpaEntity create(
            CustomerCartJpaEntity cart, long productId, int quantity) {
        var entity = new CustomerCartItemJpaEntity();
        entity.cart = cart;
        entity.key = new CartItemKey(cart.getId(), productId);
        entity.quantity = quantity;
        return entity;
    }

    long getProductId() {
        return key.getProductId();
    }
}
