package com.springbootecommerce.shophappens.administration.application.service;

import com.springbootecommerce.shophappens.administration.application.port.in.ProductAdminQuery;
import com.springbootecommerce.shophappens.administration.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.administration.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ProductAdminQueryService implements ProductAdminQuery {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductAdminView> findAll() {
        return productRepository.findAllActive().stream().map(this::toView).toList();
    }

    @Override
    public Optional<ProductAdminView> findById(long productId) {
        return productRepository.findById(new ProductId(productId)).map(this::toView);
    }

    private ProductAdminView toView(Product product) {
        Map<Long, Category> categories =
                categoryRepository.findAll().stream()
                        .filter(category -> category.id().isPresent())
                        .collect(
                                Collectors.toMap(
                                        category -> category.id().orElseThrow().value(),
                                        Function.identity()));
        return new ProductAdminView(
                product.id().orElseThrow().value(),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                product.categoryIds().stream()
                        .map(id -> categories.get(id.value()))
                        .filter(java.util.Objects::nonNull)
                        .map(
                                category ->
                                        new ProductCategorySummary(
                                                category.id().orElseThrow().value(),
                                                category.name(),
                                                category.slug()))
                        .toList());
    }
}
