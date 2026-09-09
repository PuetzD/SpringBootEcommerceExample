package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.AssignCatalogAttributeCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeAssignmentRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeDefinitionRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeAssignment;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeDefinition;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeScope;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeType;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogAttributeAssignmentServiceTest {
    private static final ProductReference FAMILY = new ProductReference(11);
    private static final ProductVariantId VARIANT = new ProductVariantId(101);
    private static final AssignCatalogAttributeCommand ASSIGN_BLUE =
            new AssignCatalogAttributeCommand("color", "blue");

    @Test
    void rejectsExistingVariantUnderAnotherFamilyWithoutWriting() {
        var definitions = mock(CatalogAttributeDefinitionRepository.class);
        var assignments = mock(CatalogAttributeAssignmentRepository.class);
        var products = mock(ProductRepository.class);
        var service = new CatalogAttributeAssignmentService(definitions, assignments, products);
        when(products.findById(new ProductId(FAMILY.value()))).thenReturn(Optional.of(family()));

        assertThatThrownBy(
                        () ->
                                service.assignToVariant(
                                        FAMILY,
                                        303,
                                        new AssignCatalogAttributeCommand("color", "blue")))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(definitions, assignments);
    }

    @Test
    void rejectsMissingParentWithoutWriting() {
        var definitions = mock(CatalogAttributeDefinitionRepository.class);
        var assignments = mock(CatalogAttributeAssignmentRepository.class);
        var products = mock(ProductRepository.class);
        var service = new CatalogAttributeAssignmentService(definitions, assignments, products);
        var missing = new ProductReference(99);
        when(products.findById(new ProductId(missing.value()))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignToVariant(missing, VARIANT.value(), ASSIGN_BLUE))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(definitions, assignments);
    }

    @Test
    void assignsAValueToAVariantOwnedByTheRequestedFamily() {
        var definitions = mock(CatalogAttributeDefinitionRepository.class);
        var assignments = mock(CatalogAttributeAssignmentRepository.class);
        var products = mock(ProductRepository.class);
        var service = new CatalogAttributeAssignmentService(definitions, assignments, products);
        var definition = colorDefinition();
        var blue = definition.allowedValues().getFirst();
        when(products.findById(new ProductId(FAMILY.value()))).thenReturn(Optional.of(family()));
        when(definitions.findByCode("color")).thenReturn(Optional.of(definition));

        service.assignToVariant(FAMILY, VARIANT.value(), ASSIGN_BLUE);

        verify(assignments).save(CatalogAttributeAssignment.select(definition, blue, VARIANT));
    }

    private static Product family() {
        var child =
                ProductVariant.restore(
                        VARIANT,
                        new Sku("TEE-A"),
                        new Money(new BigDecimal("10.00")),
                        5,
                        null,
                        true,
                        true);
        return Product.restore(
                new ProductId(FAMILY.value()), "Tee", "Cotton", true, Set.of(), List.of(child));
    }

    private static CatalogAttributeDefinition colorDefinition() {
        var definition =
                CatalogAttributeDefinition.create(
                        "color",
                        "Color",
                        CatalogAttributeType.SELECT,
                        CatalogAttributeScope.VARIANT,
                        true);
        definition.addAllowedValue("blue", "Blue");
        return definition;
    }
}
