package com.springbootecommerce.shophappens.account.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, Long> {
    boolean existsByEmail(String email);

    Optional<AccountJpaEntity> findByEmail(String email);
}
