package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import java.util.List;
import java.util.Optional;

public interface BrowseCategoriesUseCase {
    List<CategorySummary> findAllActive();

    Optional<CategorySummary> findBySlug(String slug);

    List<ProductSummary> findActiveProductsByCategorySlug(String slug);
}
