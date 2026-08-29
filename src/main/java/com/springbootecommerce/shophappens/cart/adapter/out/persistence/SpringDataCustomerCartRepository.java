package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCustomerCartRepository extends JpaRepository<CustomerCartJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<CustomerCartJpaEntity> findWithItemsByCustomerId(long customerId);

    Optional<CustomerCartJpaEntity> findByCustomerId(long customerId);
}
