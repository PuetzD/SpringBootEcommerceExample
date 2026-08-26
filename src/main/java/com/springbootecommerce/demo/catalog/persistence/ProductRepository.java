package com.springbootecommerce.demo.catalog.persistence;

import com.springbootecommerce.demo.catalog.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findBySku(String sku);

  List<Product> findByActiveTrue();

  List<Product> findByCategoriesId(Long categoryId);

  List<Product> findByActiveTrueAndCategoriesId(Long categoryId);
}
