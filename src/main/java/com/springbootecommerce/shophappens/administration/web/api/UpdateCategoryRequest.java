package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(@NotBlank(message = "Name is required") String name) {}
