package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

public record UpdateProductRequest(
        @NotNull(message = "Revision is required")
                @PositiveOrZero(message = "Revision must not be negative")
                Long revision,
        @NotBlank(message = "Name is required") String name,
        @Size(max = 2048, message = "Description must not exceed 2048 characters")
                String description,
        @NotNull(message = "Price is required")
                @DecimalMin(value = "0.00", inclusive = false, message = "Price must be positive")
                BigDecimal price,
        @NotNull(message = "Stock quantity is required")
                @PositiveOrZero(message = "Stock quantity must not be negative")
                Integer stockQuantity,
        @Size(max = 2048, message = "Image URL must not exceed 2048 characters") String imageUrl,
        @NotNull(message = "Active flag is required") Boolean active,
        Set<@jakarta.validation.constraints.Positive Long> categoryIds) {}
