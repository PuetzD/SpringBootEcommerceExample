package com.springbootecommerce.shophappens.catalog.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.math.BigDecimal;

public record CatalogAttributeAssignment(
        String definitionCode, CatalogAttributeScope scope, String value, long ownerId) {
    public static CatalogAttributeAssignment select(
            CatalogAttributeDefinition definition,
            CatalogAttributeValue value,
            ProductVariantId owner) {
        if (definition.scope() != CatalogAttributeScope.VARIANT
                || definition.type() != CatalogAttributeType.SELECT) {
            throw new IllegalArgumentException("Attribute definition is not a variant SELECT");
        }
        if (!definition.active()
                || !value.active()
                || !definition.allowedValues().contains(value)) {
            throw new IllegalArgumentException("Attribute value is not assignable");
        }
        return new CatalogAttributeAssignment(
                definition.code(), definition.scope(), value.code(), owner.value());
    }

    public static CatalogAttributeAssignment text(
            CatalogAttributeDefinition definition, ProductId owner, String value) {
        if (definition.scope() != CatalogAttributeScope.PRODUCT
                || definition.type() != CatalogAttributeType.TEXT) {
            throw new IllegalArgumentException("Attribute definition is not a product TEXT");
        }
        if (!definition.active() || value == null || value.isBlank()) {
            throw new IllegalArgumentException("Attribute value is not assignable");
        }
        return new CatalogAttributeAssignment(
                definition.code(), definition.scope(), value.strip(), owner.value());
    }

    public static CatalogAttributeAssignment number(
            CatalogAttributeDefinition definition, ProductId owner, BigDecimal value) {
        require(definition, CatalogAttributeType.NUMBER, CatalogAttributeScope.PRODUCT);
        if (!definition.active() || value == null) {
            throw new IllegalArgumentException("Attribute value is not assignable");
        }
        return new CatalogAttributeAssignment(
                definition.code(),
                definition.scope(),
                value.stripTrailingZeros().toPlainString(),
                owner.value());
    }

    public static CatalogAttributeAssignment booleanValue(
            CatalogAttributeDefinition definition, ProductId owner, boolean value) {
        require(definition, CatalogAttributeType.BOOLEAN, CatalogAttributeScope.PRODUCT);
        if (!definition.active()) {
            throw new IllegalArgumentException("Attribute value is not assignable");
        }
        return new CatalogAttributeAssignment(
                definition.code(), definition.scope(), Boolean.toString(value), owner.value());
    }

    private static void require(
            CatalogAttributeDefinition definition,
            CatalogAttributeType type,
            CatalogAttributeScope scope) {
        if (definition.scope() != scope || definition.type() != type) {
            throw new IllegalArgumentException("Attribute definition has the wrong scope or type");
        }
    }
}
