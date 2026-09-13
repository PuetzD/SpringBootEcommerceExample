package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCatalogAttributeException;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeDefinitionRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class CatalogAttributeDefinitionRepositoryAdapter implements CatalogAttributeDefinitionRepository {
    private final SpringDataCatalogAttributeDefinitionRepository definitions;
    private final CatalogAttributePersistenceMapper mapper;

    @Override
    @Transactional
    public CatalogAttributeDefinition insert(CatalogAttributeDefinition definition) {
        try {
            return mapper.toDomain(definitions.saveAndFlush(mapper.toJpa(definition)));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_catalog_attribute_definition_code"
                                .equals(violation.getConstraintName())) {
                    throw new DuplicateCatalogAttributeException(definition.code());
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public CatalogAttributeDefinition save(CatalogAttributeDefinition definition) {
        CatalogAttributeDefinitionJpaEntity entity =
                definitions
                        .findForUpdateByCode(definition.code())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Unknown attribute definition"));
        mapper.applyToJpa(entity, definition);
        definitions.flush();
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogAttributeDefinition> findByCode(String code) {
        return definitions.findByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<CatalogAttributeDefinition> findForUpdateByCode(String code) {
        return definitions.findForUpdateByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogAttributeDefinition> findAllActive() {
        return definitions.findAllByActiveTrueOrderByCodeAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
