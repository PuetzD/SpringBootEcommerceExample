package com.springbootecommerce.demo.account.persistence;

import com.springbootecommerce.demo.account.domain.Role;

public interface AccountAuthenticationProjection {
    Long getId();

    String getEmail();

    String getPasswordHash();

    Role getRole();

    boolean isEnabled();
}
