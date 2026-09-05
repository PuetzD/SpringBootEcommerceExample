package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer")
class CustomerJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    @Version
    @Column(name = "version")
    private long version;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressJpaEntity> addresses = new ArrayList<>();

    static CustomerJpaEntity create(
            Long accountId, String givenName, String familyName, String contactEmail) {
        var entity = new CustomerJpaEntity();
        entity.accountId = accountId;
        entity.givenName = givenName;
        entity.familyName = familyName;
        entity.contactEmail = contactEmail;
        return entity;
    }

    void addAddress(AddressJpaEntity address) {
        address.setCustomer(this);
        addresses.add(address);
    }
}
