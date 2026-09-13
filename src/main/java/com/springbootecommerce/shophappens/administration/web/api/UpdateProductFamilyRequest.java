package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateProductFamilyRequest(
        @NotNull @PositiveOrZero Long revision,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2048) String description,
        @NotNull Boolean active,
        Set<@Positive Long> categoryIds) {}
