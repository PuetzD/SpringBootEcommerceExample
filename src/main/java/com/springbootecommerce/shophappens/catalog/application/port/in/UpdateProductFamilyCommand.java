package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.Set;

public record UpdateProductFamilyCommand(
        String name, String description, boolean active, Set<CategoryReference> categories) {
    public UpdateProductFamilyCommand {
        categories = Set.copyOf(categories == null ? Set.of() : categories);
    }
}
