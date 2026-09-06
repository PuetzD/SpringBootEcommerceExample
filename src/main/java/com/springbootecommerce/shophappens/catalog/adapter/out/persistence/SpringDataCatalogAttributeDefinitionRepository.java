package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCatalogAttributeDefinitionRepository
        extends JpaRepository<CatalogAttributeDefinitionJpaEntity, Long> {
    @EntityGraph(attributePaths = "allowedValues")
    List<CatalogAttributeDefinitionJpaEntity> findAllByActiveTrueOrderByCodeAsc();

    @EntityGraph(attributePaths = "allowedValues")
    Optional<CatalogAttributeDefinitionJpaEntity> findByCode(String code);
}
