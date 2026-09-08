package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import java.util.List;
import java.util.Optional;

public interface CatalogAttributeDefinitionRepository {
    CatalogAttributeDefinition insert(CatalogAttributeDefinition definition);

    CatalogAttributeDefinition save(CatalogAttributeDefinition definition);

    Optional<CatalogAttributeDefinition> findByCode(String code);

    Optional<CatalogAttributeDefinition> findForUpdateByCode(String code);

    List<CatalogAttributeDefinition> findAllActive();
}
