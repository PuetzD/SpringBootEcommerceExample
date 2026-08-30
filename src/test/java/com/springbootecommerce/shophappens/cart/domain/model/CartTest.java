package com.springbootecommerce.shophappens.cart.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import org.junit.jupiter.api.Test;

class CartTest {
    private static final ProductId HEADPHONES = new ProductId(7L);

    @Test
    void keepsOneItemPerProductAndChangesIntentOnly() {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(GuestCartId.random()));

        cart.changeQuantity(HEADPHONES, new Quantity(2));
        cart.changeQuantity(HEADPHONES, new Quantity(3));

        assertThat(cart.items()).containsExactly(new CartItem(HEADPHONES, new Quantity(3)));
    }

    @Test
    void additivelyMergesMatchingProducts() {
        Cart customer = Cart.empty(CartId.random(), new CartOwner.Customer(new CustomerId(42L)));
        customer.changeQuantity(HEADPHONES, new Quantity(2));
        Cart guest = Cart.empty(CartId.random(), new CartOwner.Guest(GuestCartId.random()));
        guest.changeQuantity(HEADPHONES, new Quantity(3));
        guest.changeQuantity(new ProductId(8L), new Quantity(1));

        customer.merge(guest);

        assertThat(customer.items())
                .containsExactly(
                        new CartItem(HEADPHONES, new Quantity(5)),
                        new CartItem(new ProductId(8L), new Quantity(1)));
    }
}
