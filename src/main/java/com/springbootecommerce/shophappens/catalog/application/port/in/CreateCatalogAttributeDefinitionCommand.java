package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CreateCatalogAttributeDefinitionCommand(
        String code, String label, String type, String scope, boolean option) {}
