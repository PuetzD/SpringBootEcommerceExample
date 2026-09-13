package com.springbootecommerce.shophappens.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartConsumedException;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartWriteGuard;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
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
    @Mock GuestCartWriteGuard guard;
    @Mock CartMergeLedger ledger;
    @InjectMocks CartService service;

    @Test
    void guestAddSavesMutatedCart() {
        GuestCartId guestId = GuestCartId.random();
        Cart cart = cartWith(guestId, 5);
        when(guests.find(guestId)).thenReturn(Optional.of(cart));

        service.changeQuantity(
                new GuestCartReference(guestId.value()), new ProductVariantId(701L), 2);

        verify(guests).save(cart);
    }

    @Test
    void guestAddIncrementsWhileChangeQuantityReplaces() {
        GuestCartId guestId = GuestCartId.random();
        var variant = new ProductVariantId(202);
        Cart cart = cartWith(guestId, 5);
        cart.changeQuantity(variant, new Quantity(5));
        when(guests.find(guestId)).thenReturn(Optional.of(cart));
        var reference = new GuestCartReference(guestId.value());

        service.add(reference, variant, 1);

        assertThat(cart.items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity().value()).isEqualTo(6));
        service.changeQuantity(reference, variant, 1);
        assertThat(cart.items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity().value()).isEqualTo(1));
    }

    @Test
    void customerAddIncrementsExistingVariant() {
        CustomerId customerId = new CustomerId(42L);
        var variant = new ProductVariantId(202);
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(customerId));
        cart.changeQuantity(variant, new Quantity(2));
        when(customers.findOrCreate(customerId)).thenReturn(cart);

        service.add(customerId, variant, 3);

        assertThat(cart.items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity().value()).isEqualTo(5));
        verify(customers).save(cart);
    }

    @Test
    void guestAddOverflowLeavesCartUnchangedAndDoesNotSave() {
        GuestCartId guestId = GuestCartId.random();
        var variant = new ProductVariantId(202);
        Cart cart = cartWith(guestId, 5);
        cart.changeQuantity(variant, new Quantity(999));
        when(guests.find(guestId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> service.add(new GuestCartReference(guestId.value()), variant, 1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(cart.items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity().value()).isEqualTo(999));
        verify(guests, never()).save(cart);
    }

    @Test
    void guestAddCreatesMissingCartAndSaves() {
        GuestCartId guestId = GuestCartId.random();
        when(guests.find(guestId)).thenReturn(Optional.empty());

        service.changeQuantity(
                new GuestCartReference(guestId.value()), new ProductVariantId(701L), 2);

        verify(guests)
                .save(
                        argThat(
                                c ->
                                        c.items().stream()
                                                .anyMatch(
                                                        item ->
                                                                item.variantId().value() == 701L
                                                                        && item.quantity().value()
                                                                                == 2)));
    }

    @Test
    void guestRemoveAcquiresGuardBeforeLoadingAndSaves() {
        GuestCartId guestId = GuestCartId.random();
        var variant = new ProductVariantId(202);
        Cart cart = cartWith(guestId, 5);
        cart.changeQuantity(variant, new Quantity(1));
        when(guests.find(guestId)).thenReturn(Optional.of(cart));

        service.remove(new GuestCartReference(guestId.value()), variant);

        var protocol = org.mockito.Mockito.inOrder(guard, ledger, guests);
        protocol.verify(guard).acquire(guestId);
        protocol.verify(ledger).isConsumed(guestId);
        protocol.verify(guests).find(guestId);
        verify(guests).save(cart);
        assertThat(cart.items()).isEmpty();
    }

    @Test
    void consumedGuestRejectsMutationBeforeReadingOrWritingRedis() {
        GuestCartId guestId = GuestCartId.random();
        var reference = new GuestCartReference(guestId.value());
        when(ledger.isConsumed(guestId)).thenReturn(true);

        assertThatThrownBy(() -> service.add(reference, new ProductVariantId(202), 1))
                .isInstanceOf(GuestCartConsumedException.class);

        verify(guard).acquire(guestId);
        verify(ledger).isConsumed(guestId);
        verifyNoInteractions(guests);
    }

    @Test
    void customerAddSavesCart() {
        CustomerId customerId = new CustomerId(42L);
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(customerId));
        when(customers.findOrCreate(customerId)).thenReturn(cart);

        service.changeQuantity(new CustomerId(42L), new ProductVariantId(701L), 3);

        verify(customers).save(cart);
    }

    @Test
    void guestSnapshotMapsGuestItems() {
        GuestCartId guestId = GuestCartId.random();
        Cart cart = cartWith(guestId, 0);
        cart.changeQuantity(new ProductVariantId(701L), new Quantity(2));
        cart.changeQuantity(new ProductVariantId(801L), new Quantity(1));
        when(guests.find(guestId)).thenReturn(Optional.of(cart));
        GuestCartReference reference = new GuestCartReference(guestId.value());

        GuestCartSnapshot snapshot = service.getSnapshot(reference);

        assertThat(snapshot.guest()).isEqualTo(reference);
        assertThat(snapshot.items())
                .extracting(item -> item.variant().value())
                .containsExactly(701L, 801L);
        assertThat(snapshot.items()).extracting(item -> item.quantity()).containsExactly(2, 1);
    }

    @Test
    void customerSnapshotMapsCustomerAndItems() {
        CustomerId customerId = new CustomerId(42L);
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(customerId));
        cart.changeQuantity(new ProductVariantId(701L), new Quantity(4));
        when(customers.find(customerId)).thenReturn(Optional.of(cart));

        CustomerCartSnapshot snapshot = service.getSnapshot(new CustomerId(42L));

        assertThat(snapshot.customer().value()).isEqualTo(42L);
        assertThat(snapshot.items())
                .extracting(item -> item.variant().value())
                .containsExactly(701L);
        assertThat(snapshot.empty()).isFalse();
    }

    private static Cart cartWith(GuestCartId guestId, long version) {
        return Cart.restore(CartId.random(), new CartOwner.Guest(guestId), version);
    }
}
