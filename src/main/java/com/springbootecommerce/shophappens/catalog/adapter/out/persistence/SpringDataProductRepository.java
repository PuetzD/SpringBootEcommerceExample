package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {
    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findDetailedById(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findBySkuAndActiveTrue(String sku);

    @EntityGraph(attributePaths = "categories")
    List<ProductJpaEntity> findByActiveTrueOrderByNameAscIdAsc();
}
