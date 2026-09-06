package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.NotBlank;

public record AssignCatalogAttributeRequest(
        @NotBlank String definitionCode, @NotBlank String value) {}
