package com.springbootecommerce.shophappens.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
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
}
