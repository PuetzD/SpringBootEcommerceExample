package com.springbootecommerce.shophappens.cart.application.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.out.AfterCommitExecutor;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartMergeServiceTest {
    @Mock GuestCartRepository guests;
    @Mock CustomerCartRepository customers;
    @Mock CartMergeLedger ledger;
    @Mock AfterCommitExecutor afterCommit;
    @InjectMocks CartMergeService service;

    @Test
    void claimsMergesPersistsThenDeletesAfterCommit() {
        GuestCartId guestId = GuestCartId.random();
        Cart guest = guestCart(guestId, 7L, 3);
        Cart customer = customerCart(42L, 7L, 2);
        when(guests.find(guestId)).thenReturn(Optional.of(guest));
        when(ledger.claim(guestId, new CustomerId(42L))).thenReturn(true);
        when(customers.findOrCreate(new CustomerId(42L))).thenReturn(customer);

        service.merge(new GuestCartReference(guestId.value()), new CustomerReference(42L));

        verify(customers).save(argThat(cart -> cart.items().getFirst().quantity().value() == 5));
        verify(afterCommit).execute(argThat(action -> action != null));
    }

    @Test
    void repeatedConsumedGuestCartIsANoOp() {
        GuestCartId guestId = GuestCartId.random();
        when(guests.find(guestId)).thenReturn(Optional.of(guestCart(guestId, 7L, 3)));
        when(ledger.claim(guestId, new CustomerId(42L))).thenReturn(false);

        service.merge(new GuestCartReference(guestId.value()), new CustomerReference(42L));

        verifyNoInteractions(customers, afterCommit);
    }

    private static Cart guestCart(GuestCartId guestId, long product, int quantity) {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(guestId));
        cart.changeQuantity(new ProductId(product), new Quantity(quantity));
        return cart;
    }

    private static Cart customerCart(long customerId, long product, int quantity) {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(new CustomerId(customerId)));
        cart.changeQuantity(new ProductId(product), new Quantity(quantity));
        return cart;
    }
}
