package com.springbootecommerce.shophappens.catalog.application.port.in;

public interface ProductAdministrationUseCase {
    ProductAdminView createProduct(CreateProductCommand command);

    ProductAdminView updateProduct(
            ProductReference product,
            ProductRevision expectedRevision,
            UpdateProductCommand command);

    void deactivateProduct(ProductReference product, ProductRevision expectedRevision);
}
