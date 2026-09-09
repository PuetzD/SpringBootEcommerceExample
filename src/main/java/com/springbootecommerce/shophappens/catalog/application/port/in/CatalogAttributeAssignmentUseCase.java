package com.springbootecommerce.shophappens.catalog.application.port.in;

public interface CatalogAttributeAssignmentUseCase {
    void assignToProduct(long productId, AssignCatalogAttributeCommand command);

    void assignToVariant(
            ProductReference product, long variantId, AssignCatalogAttributeCommand command);
}
