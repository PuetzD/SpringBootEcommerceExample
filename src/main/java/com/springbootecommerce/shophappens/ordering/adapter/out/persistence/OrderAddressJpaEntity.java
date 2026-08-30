package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_address")
@IdClass(OrderAddressKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderAddressJpaEntity {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "address_role", nullable = false, length = 10)
    private AddressRole addressRole;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderJpaEntity order;

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "postal_code", nullable = false, length = 32)
    private String postalCode;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    static OrderAddressJpaEntity create(
            OrderJpaEntity order,
            AddressRole addressRole,
            String recipientName,
            String companyName,
            String addressLine1,
            String addressLine2,
            String city,
            String region,
            String postalCode,
            String countryCode,
            String phoneNumber) {
        var entity = new OrderAddressJpaEntity();
        entity.order = order;
        entity.orderId = order.getId();
        entity.addressRole = addressRole;
        entity.recipientName = recipientName;
        entity.companyName = companyName;
        entity.addressLine1 = addressLine1;
        entity.addressLine2 = addressLine2;
        entity.city = city;
        entity.region = region;
        entity.postalCode = postalCode;
        entity.countryCode = countryCode;
        entity.phoneNumber = phoneNumber;
        return entity;
    }
}
