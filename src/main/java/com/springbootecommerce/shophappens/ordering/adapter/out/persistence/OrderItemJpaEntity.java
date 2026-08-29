package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
class OrderItemJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    OrderItemJpaEntity() {}

    static OrderItemJpaEntity create(
            OrderJpaEntity order,
            long productId,
            String sku,
            String name,
            BigDecimal unitPrice,
            int quantity) {
        var entity = new OrderItemJpaEntity();
        entity.order = order;
        entity.productId = productId;
        entity.sku = sku;
        entity.name = name;
        entity.unitPrice = unitPrice;
        entity.quantity = quantity;
        return entity;
    }

    Long getId() {
        return id;
    }

    OrderJpaEntity getOrder() {
        return order;
    }

    long getProductId() {
        return productId;
    }

    String getSku() {
        return sku;
    }

    String getName() {
        return name;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }

    int getQuantity() {
        return quantity;
    }
}
