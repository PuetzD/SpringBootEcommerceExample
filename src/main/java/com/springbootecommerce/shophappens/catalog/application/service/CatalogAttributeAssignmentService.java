package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.AssignCatalogAttributeCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeAssignmentUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeAssignmentRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeDefinitionRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeAssignment;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeScope;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeType;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogAttributeAssignmentService implements CatalogAttributeAssignmentUseCase {
    private final CatalogAttributeDefinitionRepository definitions;
    private final CatalogAttributeAssignmentRepository assignments;

    @Transactional
    public void assignToProduct(long productId, AssignCatalogAttributeCommand command) {
        ProductId product = new ProductId(productId);
        CatalogAttributeDefinition definition = definition(command);
        CatalogAttributeAssignment assignment =
                productAssignment(definition, product, command.value());
        assignments.save(assignment);
    }

    @Transactional
    public void assignToVariant(long variantId, AssignCatalogAttributeCommand command) {
        ProductVariantId variant = new ProductVariantId(variantId);
        CatalogAttributeDefinition definition = definition(command);
        if (definition.type() != CatalogAttributeType.SELECT) {
            throw new IllegalArgumentException(
                    "Variant attributes currently require SELECT values");
        }
        var value =
                definition.allowedValues().stream()
                        .filter(candidate -> candidate.code().equals(command.value()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Unknown allowed attribute value"));
        assignments.save(CatalogAttributeAssignment.select(definition, value, variant));
    }

    private CatalogAttributeDefinition definition(AssignCatalogAttributeCommand command) {
        return definitions
                .findByCode(command.definitionCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown attribute definition"));
    }

    private CatalogAttributeAssignment productAssignment(
            CatalogAttributeDefinition definition, ProductId product, String value) {
        return switch (definition.type()) {
            case TEXT -> CatalogAttributeAssignment.text(definition, product, value);
            case NUMBER ->
                    CatalogAttributeAssignment.number(definition, product, new BigDecimal(value));
            case BOOLEAN ->
                    CatalogAttributeAssignment.booleanValue(
                            definition, product, Boolean.parseBoolean(value));
            case SELECT -> {
                var allowed =
                        definition.allowedValues().stream()
                                .filter(candidate -> candidate.code().equals(value))
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "Unknown allowed attribute value"));
                if (definition.scope() != CatalogAttributeScope.PRODUCT) {
                    throw new IllegalArgumentException("Attribute definition has the wrong scope");
                }
                yield new CatalogAttributeAssignment(
                        definition.code(), definition.scope(), allowed.code(), product.value());
            }
        };
    }
}
