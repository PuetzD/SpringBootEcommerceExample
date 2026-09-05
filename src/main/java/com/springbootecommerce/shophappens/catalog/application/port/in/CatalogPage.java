package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public record CatalogPage(
        List<ProductSummary> products, int page, int size, long totalElements, int totalPages) {
    public CatalogPage {
        products = List.copyOf(products);
    }
}
