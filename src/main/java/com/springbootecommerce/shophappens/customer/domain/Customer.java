package com.springbootecommerce.shophappens.customer.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column(name = "account_id")
    private Long id;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    protected Customer() {}

    private Customer(Long accountId) {
        this.id = Objects.requireNonNull(accountId);
    }

    public static Customer forAccount(Long accountId) {
        return new Customer(accountId);
    }
}
