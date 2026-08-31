package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);

    Optional<Product> findForPurchase(ProductId id);

    Optional<Product> findActiveById(ProductId id);

    Optional<Product> findActiveBySku(Sku sku);

    List<Product> findAllActive();

    long countActiveByCategoryId(CategoryId categoryId);

    List<Product> findActiveByCategoryId(CategoryId categoryId);

    Product save(Product product);
}
