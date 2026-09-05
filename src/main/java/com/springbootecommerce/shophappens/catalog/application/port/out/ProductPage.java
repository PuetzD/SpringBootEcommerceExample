package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import java.util.List;

public record ProductPage(
        List<Product> products, int page, int size, long totalElements, int totalPages) {
    public ProductPage {
        products = List.copyOf(products);
    }
}
