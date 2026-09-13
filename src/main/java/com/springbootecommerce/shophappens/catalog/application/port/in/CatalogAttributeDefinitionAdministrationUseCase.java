package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public interface CatalogAttributeDefinitionAdministrationUseCase {
    List<CatalogAttributeDefinitionView> list();

    CatalogAttributeDefinitionView create(CreateCatalogAttributeDefinitionCommand command);

    CatalogAttributeDefinitionView addAllowedValue(String code, String valueCode, String label);
}
