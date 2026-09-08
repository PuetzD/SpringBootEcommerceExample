package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCatalogAttributeDefinitionRepository
        extends JpaRepository<CatalogAttributeDefinitionJpaEntity, Long> {
    @EntityGraph(attributePaths = "allowedValues")
    List<CatalogAttributeDefinitionJpaEntity> findAllByActiveTrueOrderByCodeAsc();

    @EntityGraph(attributePaths = "allowedValues")
    Optional<CatalogAttributeDefinitionJpaEntity> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from CatalogAttributeDefinitionJpaEntity d where d.code = :code")
    Optional<CatalogAttributeDefinitionJpaEntity> findForUpdateByCode(@Param("code") String code);
}
