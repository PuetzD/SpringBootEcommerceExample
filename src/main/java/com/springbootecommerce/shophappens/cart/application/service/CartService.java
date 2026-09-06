package com.springbootecommerce.shophappens.cart.application.service;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.ClearCustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartService
        implements GuestCartUseCase,
                CustomerCartUseCase,
                CustomerCartQuery,
                ClearCustomerCartUseCase {
    private final GuestCartRepository guests;
    private final CustomerCartRepository customers;

    public void changeQuantity(GuestCartReference guest, ProductId product, int quantity) {
        changeQuantity(guest, new ProductVariantId(product.value()), quantity);
    }

    public void changeQuantity(CustomerId customer, ProductId product, int quantity) {
        changeQuantity(customer, new ProductVariantId(product.value()), quantity);
    }

    @Override
    public void changeQuantity(GuestCartReference guest, ProductVariantId variant, int quantity) {
        GuestCartId guestId = new GuestCartId(guest.value());
        Cart cart =
                guests.find(guestId)
                        .orElseGet(() -> Cart.empty(CartId.random(), new CartOwner.Guest(guestId)));
        cart.changeQuantity(variant, new Quantity(quantity));
        guests.save(cart);
    }

    @Override
    public void remove(GuestCartReference guest, ProductVariantId variant) {
        GuestCartId guestId = new GuestCartId(guest.value());
        Cart cart =
                guests.find(guestId)
                        .orElseGet(() -> Cart.empty(CartId.random(), new CartOwner.Guest(guestId)));
        cart.remove(variant);
        guests.save(cart);
    }

    @Override
    public GuestCartSnapshot getSnapshot(GuestCartReference guest) {
        Cart cart =
                guests.find(new GuestCartId(guest.value()))
                        .orElseGet(
                                () ->
                                        Cart.empty(
                                                CartId.random(),
                                                new CartOwner.Guest(
                                                        new GuestCartId(guest.value()))));
        return new GuestCartSnapshot(guest, toItemSnapshots(cart));
    }

    @Override
    @Transactional
    public void changeQuantity(CustomerId customer, ProductVariantId variant, int quantity) {
        Cart cart = customers.findOrCreate(customer);
        cart.changeQuantity(variant, new Quantity(quantity));
        customers.save(cart);
    }

    @Override
    @Transactional
    public void remove(CustomerId customer, ProductVariantId variant) {
        Cart cart = customers.findOrCreate(customer);
        cart.remove(variant);
        customers.save(cart);
    }

    @Override
    public CustomerCartSnapshot getSnapshot(CustomerId customer) {
        Cart cart =
                customers
                        .find(customer)
                        .orElseGet(
                                () ->
                                        Cart.empty(
                                                CartId.random(), new CartOwner.Customer(customer)));
        return new CustomerCartSnapshot(customer, toItemSnapshots(cart));
    }

    @Override
    public CustomerCartSnapshot get(CustomerId customer) {
        return getSnapshot(customer);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void clear(CustomerId customer) {
        customers.clear(customer);
    }

    private List<CartItemSnapshot> toItemSnapshots(Cart cart) {
        return cart.items().stream()
                .map(item -> new CartItemSnapshot(item.variantId(), item.quantity().value()))
                .toList();
    }
}
