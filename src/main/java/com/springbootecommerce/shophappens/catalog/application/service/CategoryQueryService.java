package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCategoriesUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryQueryService implements BrowseCategoriesUseCase {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryQueryService(
            CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CategorySummary> findAllActive() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::toSummary).toList();
    }

    @Override
    public Optional<CategorySummary> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug).map(this::toSummary);
    }

    @Override
    public List<ProductSummary> findActiveProductsByCategorySlug(String slug) {
        Category category =
                categoryRepository
                        .findBySlug(slug)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Category not found: " + slug));
        CategoryId categoryId = category.id().orElseThrow();
        return productRepository.findActivePageByCategoryId(categoryId, 0, 20).products().stream()
                .map(this::toSummary)
                .toList();
    }

    private CategorySummary toSummary(Category category) {
        CategoryId categoryId = category.id().orElseThrow();
        long count = productRepository.countActiveByCategoryId(categoryId);
        return new CategorySummary(
                new com.springbootecommerce.shophappens.catalog.application.port.in
                        .CategoryReference(categoryId.value()),
                category.name(),
                category.slug(),
                count);
    }

    private ProductSummary toSummary(Product product) {
        return new ProductSummary(
                new ProductReference(product.id().get().value()),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl());
    }
}
