package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.List;
import java.util.Optional;

public interface BrowseCatalogUseCase {
    CatalogPage findActivePage(int page, int size);

    List<ProductSummary> findAllActive();

    Optional<ProductSummary> findActiveById(ProductReference product);

    Optional<ProductSummary> findActiveByVariantId(ProductVariantId variant);

    Optional<ProductSummary> findActiveBySku(String sku);
}
