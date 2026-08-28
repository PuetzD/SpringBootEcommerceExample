package com.springbootecommerce.shophappens.account.persistence;

import com.springbootecommerce.shophappens.account.domain.Role;

public interface AccountAuthenticationProjection {
    Long getId();

    String getEmail();

    String getPasswordHash();

    Role getRole();

    boolean isEnabled();
}
