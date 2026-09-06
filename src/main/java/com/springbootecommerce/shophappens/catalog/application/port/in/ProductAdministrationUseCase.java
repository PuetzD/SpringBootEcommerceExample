package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.List;

public interface ProductAdministrationUseCase {
    ProductAdminView createProduct(CreateProductCommand command);

    ProductAdminView updateProduct(
            ProductReference product,
            ProductRevision expectedRevision,
            UpdateProductCommand command);

    void deactivateProduct(ProductReference product, ProductRevision expectedRevision);

    List<ProductVariantAdminView> listVariants(ProductReference product);

    ProductVariantAdminView createVariant(
            ProductReference product,
            ProductRevision expectedRevision,
            CreateProductVariantCommand command);

    ProductVariantAdminView updateVariant(
            ProductReference product,
            ProductVariantId variant,
            ProductRevision expectedRevision,
            UpdateProductVariantCommand command);

    void deleteVariant(
            ProductReference product, ProductVariantId variant, ProductRevision expectedRevision);
}
