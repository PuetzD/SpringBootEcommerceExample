package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.util.List;
import java.util.Objects;

public record OrderAdminPage(
        List<OrderAdminSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        OrderAdminMetrics metrics) {
    public OrderAdminPage {
        content = List.copyOf(content == null ? List.of() : content);
        Objects.requireNonNull(metrics, "metrics must not be null");
    }
}
