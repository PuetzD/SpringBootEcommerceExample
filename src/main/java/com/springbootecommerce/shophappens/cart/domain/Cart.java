package com.springbootecommerce.shophappens.cart.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    private Cart(Long customerId) {
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public static Cart forCustomer(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        return new Cart(customerId);
    }

    public void addProduct(Long productId, Quantity quantity) {
        CartItem existing =
                items.stream()
                        .filter(item -> item.productId().equals(productId))
                        .findFirst()
                        .orElse(null);
        if (existing != null) {
            existing.replaceQuantity(new Quantity(existing.quantity().value() + quantity.value()));
        } else {
            items.add(CartItem.of(this, productId, quantity));
        }
    }

    public void changeQuantity(Long productId, int requestedQuantity) {
        if (requestedQuantity < 0) {
            throw new IllegalArgumentException("Quantity must not be negative");
        }
        CartItem existing =
                items.stream()
                        .filter(item -> item.productId().equals(productId))
                        .findFirst()
                        .orElse(null);
        if (existing == null) {
            return;
        }
        if (requestedQuantity == 0) {
            items.remove(existing);
        } else {
            existing.replaceQuantity(new Quantity(requestedQuantity));
        }
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> items() {
        return List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
