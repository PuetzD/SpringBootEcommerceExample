package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.AmbiguousProductUpdateException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductVariantCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductVariantAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductFamilyCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductVariantCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedProduct;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.Comparator;
import java.util.List;
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
        VersionedProduct loaded = loadForUpdate(reference);
        requireRevision(reference, expectedRevision, loaded.revision());
        if (loaded.product().variants().size() > 1) {
            throw new AmbiguousProductUpdateException();
        }
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
    public ProductAdminView updateProductFamily(
            ProductReference reference,
            ProductRevision expectedRevision,
            UpdateProductFamilyCommand command) {
        VersionedProduct loaded = loadForUpdate(reference);
        requireRevision(reference, expectedRevision, loaded.revision());
        Product product = loaded.product();
        product.reviseFamilyDetails(command.name(), command.description());
        product.replaceCategories(toCategoryIds(command.categories()));
        if (command.active()) product.activate();
        else product.deactivate();
        return toAdminView(products.updateForAdministration(product, expectedRevision));
    }

    @Override
    @Transactional
    public void deactivateProduct(ProductReference reference, ProductRevision expectedRevision) {
        VersionedProduct loaded =
                products.findForAdministrationUpdate(new ProductId(reference.value()))
                        .orElseThrow(() -> new ProductNotFoundException(reference));
        loaded.product().deactivate();
        products.updateForAdministration(loaded.product(), expectedRevision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantAdminView> listVariants(ProductReference reference) {
        VersionedProduct loaded = load(reference);
        return loaded.product().variants().stream()
                .sorted(Comparator.comparingLong(variant -> variant.id().orElseThrow().value()))
                .map(variant -> toVariantView(reference, loaded.revision(), variant))
                .toList();
    }

    @Override
    @Transactional
    public ProductVariantAdminView createVariant(
            ProductReference reference,
            ProductRevision expectedRevision,
            CreateProductVariantCommand command) {
        VersionedProduct loaded = loadForUpdate(reference);
        Product product = loaded.product();
        requireRevision(reference, expectedRevision, loaded.revision());
        product.addVariant(
                ProductVariant.create(
                        new Sku(command.sku()),
                        command.price(),
                        command.stockQuantity(),
                        command.imageUrl(),
                        command.active(),
                        false));
        VersionedProduct saved = products.updateForAdministration(product, expectedRevision);
        ProductVariant variant = saved.product().variants().getLast();
        return toVariantView(reference, saved.revision(), variant);
    }

    @Override
    @Transactional
    public ProductVariantAdminView updateVariant(
            ProductReference reference,
            ProductVariantId variantId,
            ProductRevision expectedRevision,
            UpdateProductVariantCommand command) {
        VersionedProduct loaded = loadForUpdate(reference);
        requireRevision(reference, expectedRevision, loaded.revision());
        Product product = loaded.product();
        product.reviseVariant(
                variantId,
                new Sku(command.sku()),
                command.price(),
                command.stockQuantity(),
                command.imageUrl(),
                command.active());
        VersionedProduct saved = products.updateForAdministration(product, expectedRevision);
        return toVariantView(reference, saved.revision(), product.variant(variantId));
    }

    @Override
    @Transactional
    public void deleteVariant(
            ProductReference reference,
            ProductVariantId variantId,
            ProductRevision expectedRevision) {
        VersionedProduct loaded = loadForUpdate(reference);
        requireRevision(reference, expectedRevision, loaded.revision());
        Product product = loaded.product();
        product.removeVariant(product.variant(variantId));
        products.updateForAdministration(product, expectedRevision);
    }

    private VersionedProduct load(ProductReference reference) {
        return products.findForAdministration(new ProductId(reference.value()))
                .orElseThrow(() -> new ProductNotFoundException(reference));
    }

    private VersionedProduct loadForUpdate(ProductReference reference) {
        return products.findForAdministrationUpdate(new ProductId(reference.value()))
                .orElseThrow(() -> new ProductNotFoundException(reference));
    }

    private void requireRevision(
            ProductReference reference, ProductRevision expected, long actual) {
        if (expected.value() != actual) {
            throw new com.springbootecommerce.shophappens.catalog.application.port.in
                    .StaleProductRevisionException(reference, expected);
        }
    }

    private ProductVariantAdminView toVariantView(
            ProductReference product, long revision, ProductVariant variant) {
        return new ProductVariantAdminView(
                variant.id().orElseThrow(),
                product,
                variant.sku().value(),
                variant.price(),
                variant.stockQuantity(),
                variant.imageUrl(),
                variant.active(),
                variant.isDefault(),
                new ProductRevision(revision));
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
