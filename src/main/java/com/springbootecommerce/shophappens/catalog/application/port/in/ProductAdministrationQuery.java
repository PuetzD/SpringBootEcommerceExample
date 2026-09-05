package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.Optional;

public interface ProductAdministrationQuery {
    ProductAdminPage searchProducts(ProductAdminSearch search);

    Optional<ProductAdminView> findProduct(ProductReference product);
}
