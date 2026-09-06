package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public record CatalogAttributeDefinitionView(
        String code,
        String label,
        String type,
        String scope,
        boolean option,
        boolean active,
        List<CatalogAttributeValueView> allowedValues) {
    public CatalogAttributeDefinitionView {
        allowedValues = List.copyOf(allowedValues == null ? List.of() : allowedValues);
    }
}
