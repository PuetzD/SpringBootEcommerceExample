package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductId;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);

    Optional<Product> findActiveById(ProductId id);

    Optional<Product> findActiveBySku(Sku sku);

    List<Product> findAllActive();

    Product save(Product product);
}
