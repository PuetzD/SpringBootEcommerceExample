package com.springbootecommerce.shophappens.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CatalogAttributeTest {
    @Test
    void acceptsAControlledVariantValue() {
        CatalogAttributeDefinition definition =
                CatalogAttributeDefinition.create(
                        "color",
                        "Color",
                        CatalogAttributeType.SELECT,
                        CatalogAttributeScope.VARIANT,
                        true);
        CatalogAttributeValue blue = definition.addAllowedValue("blue", "Blue");

        CatalogAttributeAssignment assignment =
                CatalogAttributeAssignment.select(definition, blue, new ProductVariantId(12));

        assertThat(assignment.value()).isEqualTo("blue");
        assertThat(assignment.scope()).isEqualTo(CatalogAttributeScope.VARIANT);
    }

    @Test
    void rejectsAnInactiveAllowedValue() {
        CatalogAttributeDefinition definition =
                CatalogAttributeDefinition.create(
                        "color",
                        "Color",
                        CatalogAttributeType.SELECT,
                        CatalogAttributeScope.VARIANT,
                        true);
        CatalogAttributeValue blue = definition.addAllowedValue("blue", "Blue");
        blue.deactivate();

        assertThatThrownBy(
                        () ->
                                CatalogAttributeAssignment.select(
                                        definition, blue, new ProductVariantId(12)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizesDefinitionCodes() {
        CatalogAttributeDefinition definition =
                CatalogAttributeDefinition.create(
                        "  Material Type ",
                        "Material",
                        CatalogAttributeType.TEXT,
                        CatalogAttributeScope.PRODUCT,
                        false);

        assertThat(definition.code()).isEqualTo("material-type");
    }

    @Test
    void preservesTypedProductFacts() {
        CatalogAttributeDefinition weight =
                CatalogAttributeDefinition.create(
                        "weight",
                        "Weight",
                        CatalogAttributeType.NUMBER,
                        CatalogAttributeScope.PRODUCT,
                        false);
        CatalogAttributeDefinition recyclable =
                CatalogAttributeDefinition.create(
                        "recyclable",
                        "Recyclable",
                        CatalogAttributeType.BOOLEAN,
                        CatalogAttributeScope.PRODUCT,
                        false);

        assertThat(
                        CatalogAttributeAssignment.number(
                                weight, new ProductId(3), new BigDecimal("1.500")))
                .extracting(CatalogAttributeAssignment::value)
                .isEqualTo("1.5");
        assertThat(
                        CatalogAttributeAssignment.booleanValue(recyclable, new ProductId(3), true)
                                .value())
                .isEqualTo("true");
    }
}
