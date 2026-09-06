package com.springbootecommerce.shophappens.catalog.application.port.in;

public interface CatalogAttributeAssignmentUseCase {
    void assignToProduct(long productId, AssignCatalogAttributeCommand command);

    void assignToVariant(long variantId, AssignCatalogAttributeCommand command);
}
