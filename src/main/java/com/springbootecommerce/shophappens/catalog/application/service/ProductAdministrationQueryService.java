package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
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
public class ProductAdministrationQueryService implements ProductAdministrationQuery {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductAdminPage searchProducts(ProductAdminSearch search) {
        List<Product> products =
                productRepository.findAll().stream()
                        .filter(product -> search.active() == null || product.active() == search.active())
                        .filter(
                                product ->
                                        search.query() == null
                                                || product.name().toLowerCase().contains(search.query().toLowerCase())
                                                || product.sku().value().toLowerCase().contains(search.query().toLowerCase()))
                        .toList();
        int from = Math.min(search.page() * search.size(), products.size());
        int to = Math.min(from + search.size(), products.size());
        List<ProductAdminView> content = products.subList(from, to).stream().map(this::toView).toList();
        int totalPages = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / search.size());
        return new ProductAdminPage(content, search.page(), search.size(), products.size(), totalPages);
    }

    @Override
    public Optional<ProductAdminView> findProduct(ProductReference product) {
        return productRepository.findById(new ProductId(product.value())).map(this::toView);
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
                new ProductReference(product.id().orElseThrow().value()),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                new ProductRevision(0),
                product.categoryIds().stream()
                        .map(id -> categories.get(id.value()))
                        .filter(java.util.Objects::nonNull)
                        .map(
                                category ->
                                        new ProductCategorySummary(
                                                new CategoryReference(category.id().orElseThrow().value()),
                                                category.name(),
                                                category.slug()))
                        .toList());
    }
}
