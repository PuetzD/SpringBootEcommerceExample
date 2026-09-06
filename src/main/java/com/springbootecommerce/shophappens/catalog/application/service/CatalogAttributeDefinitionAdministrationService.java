package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeDefinitionAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeDefinitionView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeValueView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCatalogAttributeDefinitionCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeDefinitionRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeScope;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogAttributeDefinitionAdministrationService
        implements CatalogAttributeDefinitionAdministrationUseCase {
    private final CatalogAttributeDefinitionRepository definitions;

    @Transactional(readOnly = true)
    public java.util.List<CatalogAttributeDefinitionView> list() {
        return definitions.findAllActive().stream().map(this::view).toList();
    }

    @Transactional
    public CatalogAttributeDefinitionView create(CreateCatalogAttributeDefinitionCommand command) {
        return view(
                definitions.save(
                        CatalogAttributeDefinition.create(
                                command.code(),
                                command.label(),
                                CatalogAttributeType.valueOf(command.type()),
                                CatalogAttributeScope.valueOf(command.scope()),
                                command.option())));
    }

    @Transactional
    public CatalogAttributeDefinitionView addAllowedValue(
            String code, String valueCode, String label) {
        CatalogAttributeDefinition definition =
                definitions
                        .findByCode(code)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Unknown attribute definition"));
        definition.addAllowedValue(valueCode, label);
        return view(definitions.save(definition));
    }

    private CatalogAttributeDefinitionView view(CatalogAttributeDefinition definition) {
        return new CatalogAttributeDefinitionView(
                definition.code(),
                definition.label(),
                definition.type().name(),
                definition.scope().name(),
                definition.option(),
                definition.active(),
                definition.allowedValues().stream()
                        .map(
                                value ->
                                        new CatalogAttributeValueView(
                                                value.code(), value.label(), value.active()))
                        .toList());
    }
}
