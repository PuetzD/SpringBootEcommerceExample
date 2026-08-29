package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
class OrderJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(name = "customer_id", nullable = false)
    private long customerId;

    @Column(name = "shipping_recipient_name", nullable = false)
    private String shippingRecipientName;

    @Column(name = "shipping_company_name")
    private String shippingCompanyName;

    @Column(name = "shipping_address_line1", nullable = false)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_region")
    private String shippingRegion;

    @Column(name = "shipping_postal_code", nullable = false)
    private String shippingPostalCode;

    @Column(name = "shipping_country_code", nullable = false, length = 2)
    private String shippingCountryCode;

    @Column(name = "shipping_phone_number")
    private String shippingPhoneNumber;

    @Column(name = "billing_recipient_name", nullable = false)
    private String billingRecipientName;

    @Column(name = "billing_company_name")
    private String billingCompanyName;

    @Column(name = "billing_address_line1", nullable = false)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2")
    private String billingAddressLine2;

    @Column(name = "billing_city", nullable = false)
    private String billingCity;

    @Column(name = "billing_region")
    private String billingRegion;

    @Column(name = "billing_postal_code", nullable = false)
    private String billingPostalCode;

    @Column(name = "billing_country_code", nullable = false, length = 2)
    private String billingCountryCode;

    @Column(name = "billing_phone_number")
    private String billingPhoneNumber;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @Version
    @Column(name = "version")
    private long version;

    OrderJpaEntity() {}

    static OrderJpaEntity create(
            UUID id,
            String orderNumber,
            UUID checkoutId,
            long customerId,
            String shippingRecipientName,
            String shippingCompanyName,
            String shippingAddressLine1,
            String shippingAddressLine2,
            String shippingCity,
            String shippingRegion,
            String shippingPostalCode,
            String shippingCountryCode,
            String shippingPhoneNumber,
            String billingRecipientName,
            String billingCompanyName,
            String billingAddressLine1,
            String billingAddressLine2,
            String billingCity,
            String billingRegion,
            String billingPostalCode,
            String billingCountryCode,
            String billingPhoneNumber,
            Instant placedAt,
            BigDecimal total) {
        var entity = new OrderJpaEntity();
        entity.id = id;
        entity.orderNumber = orderNumber;
        entity.checkoutId = checkoutId;
        entity.customerId = customerId;
        entity.shippingRecipientName = shippingRecipientName;
        entity.shippingCompanyName = shippingCompanyName;
        entity.shippingAddressLine1 = shippingAddressLine1;
        entity.shippingAddressLine2 = shippingAddressLine2;
        entity.shippingCity = shippingCity;
        entity.shippingRegion = shippingRegion;
        entity.shippingPostalCode = shippingPostalCode;
        entity.shippingCountryCode = shippingCountryCode;
        entity.shippingPhoneNumber = shippingPhoneNumber;
        entity.billingRecipientName = billingRecipientName;
        entity.billingCompanyName = billingCompanyName;
        entity.billingAddressLine1 = billingAddressLine1;
        entity.billingAddressLine2 = billingAddressLine2;
        entity.billingCity = billingCity;
        entity.billingRegion = billingRegion;
        entity.billingPostalCode = billingPostalCode;
        entity.billingCountryCode = billingCountryCode;
        entity.billingPhoneNumber = billingPhoneNumber;
        entity.placedAt = placedAt;
        entity.total = total;
        return entity;
    }

    UUID getId() {
        return id;
    }

    String getOrderNumber() {
        return orderNumber;
    }

    UUID getCheckoutId() {
        return checkoutId;
    }

    long getCustomerId() {
        return customerId;
    }

    String getShippingRecipientName() {
        return shippingRecipientName;
    }

    String getShippingCompanyName() {
        return shippingCompanyName;
    }

    String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    String getShippingCity() {
        return shippingCity;
    }

    String getShippingRegion() {
        return shippingRegion;
    }

    String getShippingPostalCode() {
        return shippingPostalCode;
    }

    String getShippingCountryCode() {
        return shippingCountryCode;
    }

    String getShippingPhoneNumber() {
        return shippingPhoneNumber;
    }

    String getBillingRecipientName() {
        return billingRecipientName;
    }

    String getBillingCompanyName() {
        return billingCompanyName;
    }

    String getBillingAddressLine1() {
        return billingAddressLine1;
    }

    String getBillingAddressLine2() {
        return billingAddressLine2;
    }

    String getBillingCity() {
        return billingCity;
    }

    String getBillingRegion() {
        return billingRegion;
    }

    String getBillingPostalCode() {
        return billingPostalCode;
    }

    String getBillingCountryCode() {
        return billingCountryCode;
    }

    String getBillingPhoneNumber() {
        return billingPhoneNumber;
    }

    Instant getPlacedAt() {
        return placedAt;
    }

    BigDecimal getTotal() {
        return total;
    }

    List<OrderItemJpaEntity> getItems() {
        return items;
    }

    void setItems(List<OrderItemJpaEntity> items) {
        this.items.clear();
        this.items.addAll(items);
    }
}
