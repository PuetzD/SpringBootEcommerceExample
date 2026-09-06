package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateProductVariantRequest(
        @NotBlank String sku,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
        @NotNull @PositiveOrZero Integer stockQuantity,
        @Size(max = 2048) String imageUrl,
        boolean active,
        @NotNull @PositiveOrZero Long revision) {}
