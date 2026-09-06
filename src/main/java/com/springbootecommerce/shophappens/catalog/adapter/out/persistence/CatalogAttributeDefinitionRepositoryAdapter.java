package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeDefinitionRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class CatalogAttributeDefinitionRepositoryAdapter implements CatalogAttributeDefinitionRepository {
    private final SpringDataCatalogAttributeDefinitionRepository definitions;
    private final CatalogAttributePersistenceMapper mapper;

    @Override
    @Transactional
    public CatalogAttributeDefinition save(CatalogAttributeDefinition definition) {
        return mapper.toDomain(definitions.saveAndFlush(mapper.toJpa(definition)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogAttributeDefinition> findByCode(String code) {
        return definitions.findByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogAttributeDefinition> findAllActive() {
        return definitions.findAllByActiveTrueOrderByCodeAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
