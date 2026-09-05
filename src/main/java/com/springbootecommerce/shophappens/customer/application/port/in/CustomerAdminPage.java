package com.springbootecommerce.shophappens.customer.application.port.in;

import java.util.List;

public record CustomerAdminPage(
        List<CustomerAdminSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public CustomerAdminPage {
        content = List.copyOf(content == null ? List.of() : content);
    }
}
