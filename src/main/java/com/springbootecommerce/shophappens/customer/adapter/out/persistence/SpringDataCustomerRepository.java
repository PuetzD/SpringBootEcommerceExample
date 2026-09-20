package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerJpaEntity c where c.id = :id")
    Optional<CustomerJpaEntity> findRootForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "addresses")
    Optional<CustomerJpaEntity> findByAccountId(Long accountId);

    void deleteByAccountId(Long accountId);

    @Query(
            """
            select c from CustomerJpaEntity c
            where (cast(:createdFrom as timestamp) is null or c.createdAt >= :createdFrom)
              and (cast(:createdBefore as timestamp) is null or c.createdAt < :createdBefore)
              and (:query is null or :query = ''
                   or lower(c.givenName) like lower(concat('%', :query, '%')) escape :escapeCharacter
                   or lower(c.familyName) like lower(concat('%', :query, '%')) escape :escapeCharacter
                   or lower(c.contactEmail) like lower(concat('%', :query, '%')) escape :escapeCharacter)
            """)
    Page<CustomerJpaEntity> searchForAdministration(
            @Param("query") String query,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdBefore") Instant createdBefore,
            @Param("escapeCharacter") Character escapeCharacter,
            Pageable pageable);
}
