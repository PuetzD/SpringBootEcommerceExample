package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.cart.domain.exception.CartItemNotFoundException;
import java.util.List;
import java.util.TreeMap;

public final class Cart {
    private final CartId id;
    private final CartOwner owner;
    private final TreeMap<Long, CartItem> items;
    private long version;

    private Cart(CartId id, CartOwner owner, long version) {
        this.id = id;
        this.owner = owner;
        this.items = new TreeMap<>();
        this.version = version;
    }

    public static Cart empty(CartId id, CartOwner owner) {
        return new Cart(id, owner, 0);
    }

    public static Cart restore(CartId id, CartOwner owner, long version) {
        return new Cart(id, owner, version);
    }

    public CartId id() {
        return id;
    }

    public CartOwner owner() {
        return owner;
    }

    public long version() {
        return version;
    }

    public List<CartItem> items() {
        return List.copyOf(items.values());
    }

    public void changeQuantity(ProductId productId, Quantity quantity) {
        items.put(productId.value(), new CartItem(productId, quantity));
    }

    public void remove(ProductId productId) {
        if (items.remove(productId.value()) == null) {
            throw new CartItemNotFoundException(productId);
        }
    }

    public void clear() {
        items.clear();
    }

    public void merge(Cart guest) {
        if (!(owner instanceof CartOwner.Customer) || !(guest.owner instanceof CartOwner.Guest)) {
            throw new IllegalArgumentException("Merge requires Guest Cart into Customer Cart");
        }
        guest.items()
                .forEach(
                        item ->
                                items.merge(
                                        item.productId().value(),
                                        item,
                                        (current, incoming) ->
                                                new CartItem(
                                                        current.productId(),
                                                        current.quantity()
                                                                .add(incoming.quantity()))));
    }
}
