package com.springbootecommerce.shophappens.catalog.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CatalogAttributeDefinition {
    private final String code;
    private final String label;
    private final CatalogAttributeType type;
    private final CatalogAttributeScope scope;
    private final boolean option;
    private final List<CatalogAttributeValue> allowedValues = new ArrayList<>();
    private boolean active = true;

    private CatalogAttributeDefinition(
            String code,
            String label,
            CatalogAttributeType type,
            CatalogAttributeScope scope,
            boolean option) {
        this.code = CatalogAttributeValue.normalizeCode(code);
        this.label = Objects.requireNonNull(label, "label").strip();
        if (this.label.isBlank()) throw new IllegalArgumentException("Label must not be blank");
        this.type = Objects.requireNonNull(type, "type");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.option = option;
        if (option && type != CatalogAttributeType.SELECT) {
            throw new IllegalArgumentException("Options must use SELECT attributes");
        }
    }

    public static CatalogAttributeDefinition create(
            String code,
            String label,
            CatalogAttributeType type,
            CatalogAttributeScope scope,
            boolean option) {
        return new CatalogAttributeDefinition(code, label, type, scope, option);
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public CatalogAttributeType type() {
        return type;
    }

    public CatalogAttributeScope scope() {
        return scope;
    }

    public boolean option() {
        return option;
    }

    public boolean active() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public List<CatalogAttributeValue> allowedValues() {
        return List.copyOf(allowedValues);
    }

    public CatalogAttributeValue addAllowedValue(String code, String label) {
        if (type != CatalogAttributeType.SELECT) {
            throw new IllegalArgumentException("Only SELECT attributes have allowed values");
        }
        CatalogAttributeValue value = new CatalogAttributeValue(code, label);
        if (allowedValues.stream().anyMatch(existing -> existing.code().equals(value.code()))) {
            throw new IllegalArgumentException("Allowed value code must be unique");
        }
        allowedValues.add(value);
        return value;
    }
}
