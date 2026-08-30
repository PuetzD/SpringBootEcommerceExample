package com.springbootecommerce.shophappens.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock GuestCartRepository guests;
    @Mock CustomerCartRepository customers;
    @InjectMocks CartService service;

    @Test
    void guestAddSavesMutatedCart() {
        GuestCartId guestId = GuestCartId.random();
        Cart cart = cartWith(guestId, 5);
        when(guests.find(guestId)).thenReturn(Optional.of(cart));

        service.changeQuantity(
                new GuestCartReference(guestId.value()), new ProductReference(7L), 2);

        verify(guests).save(cart);
    }

    @Test
    void guestAddCreatesMissingCartAndSaves() {
        GuestCartId guestId = GuestCartId.random();
        when(guests.find(guestId)).thenReturn(Optional.empty());

        service.changeQuantity(
                new GuestCartReference(guestId.value()), new ProductReference(7L), 2);

        verify(guests)
                .save(
                        argThat(
                                c ->
                                        c.items().stream()
                                                .anyMatch(
                                                        item ->
                                                                item.productId().value() == 7L
                                                                        && item.quantity().value()
                                                                                == 2)));
    }

    @Test
    void customerAddSavesCart() {
        CustomerId customerId = new CustomerId(42L);
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(customerId));
        when(customers.findOrCreate(customerId)).thenReturn(cart);

        service.changeQuantity(new CustomerReference(42L), new ProductReference(7L), 3);

        verify(customers).save(cart);
    }

    @Test
    void guestSnapshotMapsGuestItems() {
        GuestCartId guestId = GuestCartId.random();
        Cart cart = cartWith(guestId, 0);
        cart.changeQuantity(new ProductId(7L), new Quantity(2));
        cart.changeQuantity(new ProductId(8L), new Quantity(1));
        when(guests.find(guestId)).thenReturn(Optional.of(cart));
        GuestCartReference reference = new GuestCartReference(guestId.value());

        GuestCartSnapshot snapshot = service.getSnapshot(reference);

        assertThat(snapshot.guest()).isEqualTo(reference);
        assertThat(snapshot.items())
                .extracting(item -> item.product().value())
                .containsExactly(7L, 8L);
        assertThat(snapshot.items()).extracting(item -> item.quantity()).containsExactly(2, 1);
    }

    @Test
    void customerSnapshotMapsCustomerAndItems() {
        CustomerId customerId = new CustomerId(42L);
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(customerId));
        cart.changeQuantity(new ProductId(7L), new Quantity(4));
        when(customers.find(customerId)).thenReturn(Optional.of(cart));

        CustomerCartSnapshot snapshot = service.getSnapshot(new CustomerReference(42L));

        assertThat(snapshot.customer().value()).isEqualTo(42L);
        assertThat(snapshot.items()).extracting(item -> item.product().value()).containsExactly(7L);
        assertThat(snapshot.empty()).isFalse();
    }

    private static Cart cartWith(GuestCartId guestId, long version) {
        return Cart.restore(CartId.random(), new CartOwner.Guest(guestId), version);
    }
}
