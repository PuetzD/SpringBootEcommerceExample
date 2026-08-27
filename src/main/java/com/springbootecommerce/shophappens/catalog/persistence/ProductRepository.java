package com.springbootecommerce.shophappens.catalog.persistence;

import com.springbootecommerce.shophappens.catalog.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Optional<Product> findBySkuAndActiveTrue(String sku);

    List<Product> findByActiveTrueOrderByNameAscIdAsc();
}
