package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findDetailedById(Long id);

    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findByAccountId(Long accountId);

    @Query(
            """
            select c from CustomerJpaEntity c
            where (:query is null or :query = ''
                   or lower(c.givenName) like lower(concat('%', :query, '%')) escape '\\'
                   or lower(c.familyName) like lower(concat('%', :query, '%')) escape '\\'
                   or lower(c.contactEmail) like lower(concat('%', :query, '%')) escape '\\')
            """)
    Page<CustomerJpaEntity> searchForAdministration(
            @Param("query") String query, Pageable pageable);
}
