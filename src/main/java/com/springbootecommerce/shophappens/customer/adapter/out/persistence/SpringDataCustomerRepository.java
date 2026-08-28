package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findDetailedById(Long id);

    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findByAccountId(Long accountId);
}
