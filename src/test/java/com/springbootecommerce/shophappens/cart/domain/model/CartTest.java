package com.springbootecommerce.shophappens.cart.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import org.junit.jupiter.api.Test;

class CartTest {
    private static final ProductVariantId HEADPHONES = new ProductVariantId(701L);

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
        guest.changeQuantity(new ProductVariantId(801L), new Quantity(1));

        customer.merge(guest);

        assertThat(customer.items())
                .containsExactly(
                        new CartItem(HEADPHONES, new Quantity(5)),
                        new CartItem(new ProductVariantId(801L), new Quantity(1)));
    }

    @Test
    void addAndSetHaveDifferentMeaningsAndDoNotTouchSiblings() {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(GuestCartId.random()));
        var small = new ProductVariantId(101);
        var large = new ProductVariantId(202);
        cart.changeQuantity(small, new Quantity(5));
        cart.changeQuantity(large, new Quantity(2));

        cart.add(small, new Quantity(1));

        assertThat(cart.items())
                .containsExactly(
                        new CartItem(small, new Quantity(6)), new CartItem(large, new Quantity(2)));

        cart.changeQuantity(small, new Quantity(1));
        assertThat(cart.items())
                .containsExactly(
                        new CartItem(small, new Quantity(1)), new CartItem(large, new Quantity(2)));

        cart.changeQuantity(small, new Quantity(999));
        assertThatThrownBy(() -> cart.add(small, new Quantity(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(cart.items().getFirst().quantity().value()).isEqualTo(999);
    }
}
