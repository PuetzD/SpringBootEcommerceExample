package com.springbootecommerce.shophappens.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void addsAndMergesOneItemPerProduct() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(1));
        cart.addProduct(7L, new Quantity(2));

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().getFirst().quantity()).isEqualTo(new Quantity(3));
    }

    @Test
    void changesPositiveQuantityAndUsesZeroAsRemovalCommand() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(2));
        cart.changeQuantity(7L, 4);
        assertThat(cart.items().getFirst().quantity()).isEqualTo(new Quantity(4));

        cart.changeQuantity(7L, 0);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void rejectsNegativeChangeAndClearsAllItems() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(1));
        assertThatThrownBy(() -> cart.changeQuantity(7L, -1))
                .isInstanceOf(IllegalArgumentException.class);
        cart.clear();
        assertThat(cart.isEmpty()).isTrue();
    }
}
