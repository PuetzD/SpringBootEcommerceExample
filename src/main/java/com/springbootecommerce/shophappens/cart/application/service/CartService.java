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
import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService
        implements GuestCartUseCase,
                CustomerCartUseCase,
                CustomerCartQuery,
                ClearCustomerCartUseCase {
    private final GuestCartRepository guests;
    private final CustomerCartRepository customers;

    public CartService(GuestCartRepository guests, CustomerCartRepository customers) {
        this.guests = guests;
        this.customers = customers;
    }

    @Override
    public void changeQuantity(GuestCartReference guest, ProductReference product, int quantity) {
        GuestCartId guestId = new GuestCartId(guest.value());
        Cart cart =
                guests.find(guestId)
                        .orElseGet(() -> Cart.empty(CartId.random(), new CartOwner.Guest(guestId)));
        cart.changeQuantity(new ProductId(product.value()), new Quantity(quantity));
        guests.save(cart);
    }

    @Override
    public void remove(GuestCartReference guest, ProductReference product) {
        GuestCartId guestId = new GuestCartId(guest.value());
        Cart cart =
                guests.find(guestId)
                        .orElseGet(() -> Cart.empty(CartId.random(), new CartOwner.Guest(guestId)));
        cart.remove(new ProductId(product.value()));
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
    public void changeQuantity(CustomerReference customer, ProductReference product, int quantity) {
        CustomerId customerId = new CustomerId(customer.value());
        Cart cart = customers.findOrCreate(customerId);
        cart.changeQuantity(new ProductId(product.value()), new Quantity(quantity));
        customers.save(cart);
    }

    @Override
    @Transactional
    public void remove(CustomerReference customer, ProductReference product) {
        CustomerId customerId = new CustomerId(customer.value());
        Cart cart = customers.findOrCreate(customerId);
        cart.remove(new ProductId(product.value()));
        customers.save(cart);
    }

    @Override
    public CustomerCartSnapshot getSnapshot(CustomerReference customer) {
        Cart cart =
                customers
                        .find(new CustomerId(customer.value()))
                        .orElseGet(
                                () ->
                                        Cart.empty(
                                                CartId.random(),
                                                new CartOwner.Customer(
                                                        new CustomerId(customer.value()))));
        return new CustomerCartSnapshot(customer, toItemSnapshots(cart));
    }

    @Override
    public CustomerCartSnapshot get(CustomerReference customer) {
        return getSnapshot(customer);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void clear(CustomerReference customer) {
        customers.clear(new CustomerId(customer.value()));
    }

    private List<CartItemSnapshot> toItemSnapshots(Cart cart) {
        return cart.items().stream()
                .map(
                        item ->
                                new CartItemSnapshot(
                                        new ProductReference(item.productId().value()),
                                        item.quantity().value()))
                .toList();
    }
}
