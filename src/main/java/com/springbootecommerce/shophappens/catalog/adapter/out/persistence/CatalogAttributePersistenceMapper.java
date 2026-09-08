package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class CatalogAttributePersistenceMapper {
    CatalogAttributeDefinitionJpaEntity toJpa(CatalogAttributeDefinition definition) {
        CatalogAttributeDefinitionJpaEntity entity =
                CatalogAttributeDefinitionJpaEntity.create(
                        definition.code(),
                        definition.label(),
                        definition.type(),
                        definition.scope(),
                        definition.option());
        entity.setActive(definition.active());
        Set<CatalogAttributeValueJpaEntity> values = new LinkedHashSet<>();
        definition
                .allowedValues()
                .forEach(
                        value -> {
                            CatalogAttributeValueJpaEntity valueEntity =
                                    CatalogAttributeValueJpaEntity.create(
                                            entity, value.code(), value.label());
                            valueEntity.setActive(value.active());
                            values.add(valueEntity);
                        });
        entity.setAllowedValues(values);
        return entity;
    }

    CatalogAttributeDefinition toDomain(CatalogAttributeDefinitionJpaEntity entity) {
        CatalogAttributeDefinition definition =
                CatalogAttributeDefinition.create(
                        entity.getCode(),
                        entity.getLabel(),
                        entity.getType(),
                        entity.getScope(),
                        entity.isOption());
        entity.getAllowedValues()
                .forEach(
                        value -> {
                            var domainValue =
                                    definition.addAllowedValue(value.getCode(), value.getLabel());
                            if (!value.isActive()) domainValue.deactivate();
                        });
        if (!entity.isActive()) definition.deactivate();
        return definition;
    }

    void applyToJpa(
            CatalogAttributeDefinitionJpaEntity entity, CatalogAttributeDefinition definition) {
        entity.setActive(definition.active());
        var existing =
                entity.getAllowedValues().stream()
                        .collect(
                                Collectors.toMap(
                                        CatalogAttributeValueJpaEntity::getCode,
                                        Function.identity()));
        for (var value : definition.allowedValues()) {
            var stored = existing.get(value.code());
            if (stored == null) {
                stored = CatalogAttributeValueJpaEntity.create(entity, value.code(), value.label());
                entity.getAllowedValues().add(stored);
            }
            stored.setActive(value.active());
        }
    }
}
