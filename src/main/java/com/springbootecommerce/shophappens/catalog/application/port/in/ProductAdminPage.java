package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public record ProductAdminPage(
        List<ProductAdminView> content, int page, int size, long totalElements, int totalPages) {
    public ProductAdminPage {
        content = List.copyOf(content == null ? List.of() : content);
    }
}
