package com.springbootecommerce.demo.account.persistence;

import com.springbootecommerce.demo.account.domain.Role;

public interface AccountAuthenticationProjection {
  String getEmail();

  String getPasswordHash();

  Role getRole();

  boolean isEnabled();
}
