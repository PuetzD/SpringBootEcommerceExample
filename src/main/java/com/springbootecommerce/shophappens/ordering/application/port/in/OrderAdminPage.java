package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.util.List;

public record OrderAdminPage(
        List<OrderAdminDetail> content, int page, int size, long totalElements, int totalPages) {
    public OrderAdminPage {
        content = List.copyOf(content == null ? List.of() : content);
    }
}
