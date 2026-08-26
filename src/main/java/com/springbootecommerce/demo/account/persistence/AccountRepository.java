package com.springbootecommerce.demo.account.persistence;

import com.springbootecommerce.demo.account.domain.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<AccountAuthenticationProjection> findByEmailIgnoreCase(String email);
}
