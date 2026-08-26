package com.springbootecommerce.demo.catalog.application;

import com.springbootecommerce.demo.catalog.domain.Category;
import com.springbootecommerce.demo.catalog.domain.Product;
import com.springbootecommerce.demo.catalog.persistence.CategoryRepository;
import com.springbootecommerce.demo.catalog.persistence.ProductRepository;
import com.springbootecommerce.demo.catalog.web.CategoryView;
import com.springbootecommerce.demo.catalog.web.ProductView;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  public CatalogQueryService(
      ProductRepository productRepository, CategoryRepository categoryRepository) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
  }

  public List<ProductView> findAllActiveProducts() {
    return productRepository.findByActiveTrue().stream().map(this::toProductView).toList();
  }

  public List<ProductView> findProductsByCategory(Long categoryId) {
    return productRepository.findByActiveTrueAndCategoriesId(categoryId).stream()
        .map(this::toProductView)
        .toList();
  }

  public Optional<ProductView> findProductBySku(String sku) {
    return productRepository.findBySku(sku).map(this::toProductView);
  }

  public List<CategoryView> findAllCategories() {
    return categoryRepository.findAll().stream().map(this::toCategoryView).toList();
  }

  public Optional<CategoryView> findCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug).map(this::toCategoryView);
  }

  private ProductView toProductView(Product product) {
    var primaryCategory =
        product.getCategories().stream().findFirst().map(Category::getName).orElse("");
    return new ProductView(
        product.getId(),
        product.getSku(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStockQuantity(),
        product.getImageUrl(),
        product.isActive());
  }

  private CategoryView toCategoryView(Category category) {
    return new CategoryView(category.getId(), category.getName(), category.getSlug());
  }
}
