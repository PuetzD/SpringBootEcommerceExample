package com.springbootecommerce.shophappens.cart.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    protected CartItem() {}

    private CartItem(Cart cart, Long productId, Quantity quantity) {
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
    }

    static CartItem of(Cart cart, Long productId, Quantity quantity) {
        return new CartItem(cart, productId, quantity);
    }

    public Long productId() {
        return productId;
    }

    public Quantity quantity() {
        return quantity;
    }

    void replaceQuantity(Quantity quantity) {
        this.quantity = quantity;
    }
}
