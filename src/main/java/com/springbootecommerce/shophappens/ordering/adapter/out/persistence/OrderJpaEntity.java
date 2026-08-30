package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "order_number", nullable = false, length = 32)
    private String orderNumber;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(name = "customer_id", nullable = false)
    private long customerId;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<OrderAddressJpaEntity> addresses = new ArrayList<>();

    static OrderJpaEntity fromId(
            UUID id,
            String orderNumber,
            UUID checkoutId,
            long customerId,
            BigDecimal total,
            Instant placedAt) {
        var entity = new OrderJpaEntity();
        entity.id = id;
        entity.orderNumber = orderNumber;
        entity.checkoutId = checkoutId;
        entity.customerId = customerId;
        entity.total = total;
        entity.placedAt = placedAt;
        return entity;
    }

    void setItems(List<OrderItemJpaEntity> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    void setAddresses(List<OrderAddressJpaEntity> addresses) {
        this.addresses.clear();
        this.addresses.addAll(addresses);
    }
}
