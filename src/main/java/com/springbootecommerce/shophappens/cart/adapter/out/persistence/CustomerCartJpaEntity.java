package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_cart")
class CustomerCartJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private long customerId;

    @Version
    @Column(name = "version")
    private long version;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerCartItemJpaEntity> items = new ArrayList<>();

    CustomerCartJpaEntity() {}

    static CustomerCartJpaEntity create(UUID id, long customerId) {
        var entity = new CustomerCartJpaEntity();
        entity.id = id;
        entity.customerId = customerId;
        return entity;
    }

    UUID getId() {
        return id;
    }

    long getCustomerId() {
        return customerId;
    }

    long getVersion() {
        return version;
    }

    List<CustomerCartItemJpaEntity> getItems() {
        return items;
    }

    void setItems(List<CustomerCartItemJpaEntity> items) {
        this.items.clear();
        this.items.addAll(items);
    }
}
