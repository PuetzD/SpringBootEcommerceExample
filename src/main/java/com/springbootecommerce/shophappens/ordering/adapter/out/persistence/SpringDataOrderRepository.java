package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByCheckoutIdAndCustomerId(UUID checkoutId, long customerId);

    Optional<OrderJpaEntity> findByCustomerIdAndOrderNumber(long customerId, String orderNumber);

    List<OrderJpaEntity> findByCustomerIdOrderByPlacedAtDescIdDesc(long customerId);
}
