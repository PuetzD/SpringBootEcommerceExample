package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCatalogAttributeDefinitionRequest(
        @NotBlank String code,
        @NotBlank String label,
        @NotNull String type,
        @NotNull String scope,
        boolean option) {}
