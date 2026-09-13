package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.NotBlank;

public record CreateCatalogAttributeValueRequest(@NotBlank String code, @NotBlank String label) {}
