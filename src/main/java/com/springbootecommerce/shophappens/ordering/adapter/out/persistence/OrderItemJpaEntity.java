package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_item")
@IdClass(OrderItemKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderItemJpaEntity {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Id
    @Column(name = "line_number")
    private int lineNumber;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    static OrderItemJpaEntity create(
            OrderJpaEntity order,
            int lineNumber,
            long productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {
        var entity = new OrderItemJpaEntity();
        entity.order = order;
        entity.orderId = order.getId();
        entity.lineNumber = lineNumber;
        entity.productId = productId;
        entity.sku = sku;
        entity.productName = productName;
        entity.unitPrice = unitPrice;
        entity.quantity = quantity;
        entity.lineTotal = lineTotal;
        return entity;
    }
}
