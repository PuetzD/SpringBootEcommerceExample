package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public record CategoryAdminPage(
        List<CategoryAdminView> content, int page, int size, long totalElements, int totalPages) {
    public CategoryAdminPage {
        content = List.copyOf(content == null ? List.of() : content);
    }
}
