package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.cart.domain.exception.CartItemNotFoundException;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
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

    public void changeQuantity(ProductVariantId variantId, Quantity quantity) {
        items.put(variantId.value(), new CartItem(variantId, quantity));
    }

    public void add(ProductVariantId variantId, Quantity added) {
        CartItem current = items.get(variantId.value());
        Quantity result = current == null ? added : current.quantity().add(added);
        items.put(variantId.value(), new CartItem(variantId, result));
    }

    public void changeQuantity(ProductId productId, Quantity quantity) {
        changeQuantity(new ProductVariantId(productId.value()), quantity);
    }

    public void remove(ProductVariantId variantId) {
        if (items.remove(variantId.value()) == null) {
            throw new CartItemNotFoundException(variantId);
        }
    }

    public void remove(ProductId productId) {
        remove(new ProductVariantId(productId.value()));
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
                                        item.variantId().value(),
                                        item,
                                        (current, incoming) ->
                                                new CartItem(
                                                        current.variantId(),
                                                        current.quantity()
                                                                .add(incoming.quantity()))));
    }
}
