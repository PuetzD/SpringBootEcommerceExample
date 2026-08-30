package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AddressJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "postal_code", nullable = false, length = 32)
    private String postalCode;

    @Column(name = "country_code", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String countryCode;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "is_default_shipping", nullable = false)
    private boolean defaultShipping;

    @Column(name = "is_default_billing", nullable = false)
    private boolean defaultBilling;

    @Version
    @Column(name = "version")
    private long version;

    static AddressJpaEntity create(
            String recipientName,
            String companyName,
            String addressLine1,
            String addressLine2,
            String city,
            String region,
            String postalCode,
            String countryCode,
            String phoneNumber,
            boolean defaultShipping,
            boolean defaultBilling) {
        var entity = new AddressJpaEntity();
        entity.recipientName = recipientName;
        entity.companyName = companyName;
        entity.addressLine1 = addressLine1;
        entity.addressLine2 = addressLine2;
        entity.city = city;
        entity.region = region;
        entity.postalCode = postalCode;
        entity.countryCode = countryCode;
        entity.phoneNumber = phoneNumber;
        entity.defaultShipping = defaultShipping;
        entity.defaultBilling = defaultBilling;
        return entity;
    }
}
