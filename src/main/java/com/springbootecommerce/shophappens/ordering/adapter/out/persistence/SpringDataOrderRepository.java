package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByCheckoutIdAndCustomerId(UUID checkoutId, long customerId);

    Optional<OrderJpaEntity> findByCustomerIdAndOrderNumber(long customerId, String orderNumber);

    List<OrderJpaEntity> findByCustomerIdOrderByPlacedAtDescIdDesc(long customerId);

    @Query(
            """
            select o from OrderJpaEntity o
            where (:query is null or :query = ''
                   or lower(o.orderNumber) like lower(concat('%', :query, '%')))
            order by o.placedAt desc, o.id desc
            """)
    Page<OrderJpaEntity> searchForAdministration(@Param("query") String query, Pageable pageable);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);
}
