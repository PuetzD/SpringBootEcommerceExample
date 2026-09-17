package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.time.Instant;
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
              and (cast(:placedFrom as timestamp) is null or o.placedAt >= :placedFrom)
              and (cast(:placedBefore as timestamp) is null or o.placedAt < :placedBefore)
            order by o.placedAt desc, o.id desc
            """)
    Page<OrderJpaEntity> searchForAdministration(
            @Param("query") String query,
            @Param("placedFrom") Instant placedFrom,
            @Param("placedBefore") Instant placedBefore,
            Pageable pageable);

    @Query(
            """
            select coalesce(sum(o.total), 0) as revenue from OrderJpaEntity o
            where (:query is null or :query = ''
                   or lower(o.orderNumber) like lower(concat('%', :query, '%')))
              and (cast(:placedFrom as timestamp) is null or o.placedAt >= :placedFrom)
              and (cast(:placedBefore as timestamp) is null or o.placedAt < :placedBefore)
            """)
    OrderMetricsProjection summarizeForAdministration(
            @Param("query") String query,
            @Param("placedFrom") Instant placedFrom,
            @Param("placedBefore") Instant placedBefore);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);
}
