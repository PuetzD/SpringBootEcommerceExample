package com.springbootecommerce.shophappens.account.domain.model;

public enum Role {
    CUSTOMER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
