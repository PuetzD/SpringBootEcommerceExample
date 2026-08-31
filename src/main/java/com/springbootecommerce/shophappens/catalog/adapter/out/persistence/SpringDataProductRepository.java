package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {
    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductJpaEntity p where p.id = :id")
    Optional<ProductJpaEntity> findForPurchaseById(@Param("id") Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findBySkuAndActiveTrue(String sku);

    @EntityGraph(attributePaths = "categories")
    List<ProductJpaEntity> findByActiveTrueOrderByNameAscIdAsc();

    @Query(
            "select count(distinct p) from ProductJpaEntity p join p.categories c where c.id = :categoryId and p.active = true")
    long countActiveByCategoryId(Long categoryId);

    @Query(
            "select distinct p from ProductJpaEntity p join p.categories c where c.id = :categoryId and p.active = true order by p.name asc, p.id asc")
    List<ProductJpaEntity> findActiveByCategoryId(Long categoryId);
}
