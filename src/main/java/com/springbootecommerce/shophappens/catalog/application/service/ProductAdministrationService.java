package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedProduct;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductAdministrationService implements ProductAdministrationUseCase {
    private final ProductRepository products;
    private final CategoryRepository categories;

    @Override
    @Transactional
    public ProductAdminView createProduct(CreateProductCommand command) {
        Product product =
                Product.create(
                        new Sku(command.sku()),
                        command.name(),
                        command.description(),
                        command.price(),
                        command.stockQuantity(),
                        command.imageUrl(),
                        toCategoryIds(command.categories()));
        return toAdminView(products.insertForAdministration(product));
    }

    @Override
    @Transactional
    public ProductAdminView updateProduct(
            ProductReference reference,
            ProductRevision expectedRevision,
            UpdateProductCommand command) {
        VersionedProduct loaded =
                products.findForAdministration(new ProductId(reference.value()))
                        .orElseThrow(() -> new ProductNotFoundException(reference));
        Product product = loaded.product();
        product.reviseDetails(
                command.name(), command.description(), command.price(), command.imageUrl());
        product.setStockQuantity(command.stockQuantity());
        product.replaceCategories(toCategoryIds(command.categories()));
        if (command.active()) product.activate();
        else product.deactivate();
        return toAdminView(products.updateForAdministration(product, expectedRevision));
    }

    @Override
    @Transactional
    public void deactivateProduct(ProductReference reference, ProductRevision expectedRevision) {
        VersionedProduct loaded =
                products.findForAdministration(new ProductId(reference.value()))
                        .orElseThrow(() -> new ProductNotFoundException(reference));
        loaded.product().deactivate();
        products.updateForAdministration(loaded.product(), expectedRevision);
    }

    private Set<CategoryId> toCategoryIds(Set<CategoryReference> references) {
        return references.stream()
                .map(reference -> new CategoryId(reference.value()))
                .collect(Collectors.toSet());
    }

    private ProductAdminView toAdminView(VersionedProduct versioned) {
        Product product = versioned.product();
        var categoryViews =
                categories.findAll().stream()
                        .filter(
                                category ->
                                        product.categoryIds().contains(category.id().orElseThrow()))
                        .map(
                                category ->
                                        new ProductCategorySummary(
                                                new CategoryReference(
                                                        category.id().orElseThrow().value()),
                                                category.name(),
                                                category.slug()))
                        .toList();
        return new ProductAdminView(
                new ProductReference(product.id().orElseThrow().value()),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                new ProductRevision(versioned.revision()),
                categoryViews);
    }
}
