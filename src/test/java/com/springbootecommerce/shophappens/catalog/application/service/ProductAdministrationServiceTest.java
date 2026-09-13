package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.AmbiguousProductUpdateException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedProduct;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProductAdministrationServiceTest {

    private static final ProductReference PRODUCT = new ProductReference(101L);
    private static final ProductRevision REVISION = new ProductRevision(4L);

    private final ProductRepository products = mock(ProductRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final ProductAdministrationService service =
            new ProductAdministrationService(products, categories);

    @Test
    void createsDefaultVariantAndReturnsInsertedView() {
        Product saved = existingProduct();
        when(products.insertForAdministration(any(Product.class)))
                .thenReturn(new VersionedProduct(saved, 0L));
        when(categories.findAll()).thenReturn(List.of(category(10L, "Tools", "tools")));

        var result = service.createProduct(createCommand());

        ArgumentCaptor<Product> inserted = ArgumentCaptor.forClass(Product.class);
        verify(products).insertForAdministration(inserted.capture());
        Product created = inserted.getValue();
        assertThat(created.id()).isEmpty();
        assertThat(created.sku()).isEqualTo(new Sku("ORIGINAL-SKU"));
        assertThat(created.name()).isEqualTo("Widget");
        assertThat(created.description()).isEqualTo("Useful widget");
        assertThat(created.price()).isEqualTo(money("19.99"));
        assertThat(created.stockQuantity()).isEqualTo(7);
        assertThat(created.imageUrl()).isEqualTo("https://example.com/widget.png");
        assertThat(created.active()).isTrue();
        assertThat(created.categoryIds()).containsExactly(new CategoryId(10L));
        assertThat(created.variants())
                .singleElement()
                .satisfies(
                        variant -> {
                            assertThat(variant.sku()).isEqualTo(new Sku("ORIGINAL-SKU"));
                            assertThat(variant.isDefault()).isTrue();
                        });
        assertThat(result.product()).isEqualTo(PRODUCT);
        assertThat(result.revision()).isEqualTo(new ProductRevision(0L));
        assertThat(result.categories())
                .containsExactly(
                        new ProductCategorySummary(new CategoryReference(10L), "Tools", "tools"));
        verify(categories).findAll();
    }

    @Test
    void updatesDefaultVariantWithoutChangingItsSku() {
        Product loaded = existingProduct();
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.of(new VersionedProduct(loaded, REVISION.value())));
        when(products.updateForAdministration(any(Product.class), eq(REVISION)))
                .thenAnswer(invocation -> new VersionedProduct(invocation.getArgument(0), 5L));
        when(categories.findAll()).thenReturn(List.of(category(12L, "Home", "home")));

        var result = service.updateProduct(PRODUCT, REVISION, updateCommand(false, 11));

        ArgumentCaptor<Product> updated = ArgumentCaptor.forClass(Product.class);
        verify(products).updateForAdministration(updated.capture(), eq(REVISION));
        Product persisted = updated.getValue();
        assertThat(persisted.sku()).isEqualTo(new Sku("ORIGINAL-SKU"));
        assertThat(persisted.name()).isEqualTo("Updated widget");
        assertThat(persisted.description()).isEqualTo("Updated description");
        assertThat(persisted.price()).isEqualTo(money("29.99"));
        assertThat(persisted.stockQuantity()).isEqualTo(11);
        assertThat(persisted.imageUrl()).isEqualTo("https://example.com/updated.png");
        assertThat(persisted.active()).isFalse();
        assertThat(persisted.categoryIds()).containsExactly(new CategoryId(12L));
        assertThat(result.revision()).isEqualTo(new ProductRevision(5L));
        assertThat(result.categories())
                .containsExactly(
                        new ProductCategorySummary(new CategoryReference(12L), "Home", "home"));
    }

    @Test
    void rejectsLegacyCommercialUpdateForVariantFamily() {
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.of(new VersionedProduct(productFamily(), REVISION.value())));

        assertThatThrownBy(() -> service.updateProduct(PRODUCT, REVISION, updateCommand(true, 11)))
                .isInstanceOf(AmbiguousProductUpdateException.class);
        verify(products, never()).updateForAdministration(any(Product.class), any());
    }

    @Test
    void missingProductBecomesPublishedNotFoundFailure() {
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduct(PRODUCT, REVISION, updateCommand(true, 11)))
                .isInstanceOf(ProductNotFoundException.class)
                .satisfies(
                        exception ->
                                assertThat(((ProductNotFoundException) exception).product())
                                        .isEqualTo(PRODUCT));
    }

    @Test
    void translatesBlankProductCreationToPublishedCatalogFailure() {
        CreateProductCommand command =
                new CreateProductCommand(
                        "ORIGINAL-SKU", " ", "Useful widget", money("19.99"), 7, null, Set.of());

        assertThatThrownBy(() -> service.createProduct(command))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("Name must not be blank");
    }

    @Test
    void translatesNegativeProductCreationToPublishedCatalogFailure() {
        CreateProductCommand command =
                new CreateProductCommand(
                        "ORIGINAL-SKU",
                        "Widget",
                        "Useful widget",
                        money("19.99"),
                        -1,
                        null,
                        Set.of());

        assertThatThrownBy(() -> service.createProduct(command))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("Initial stock must not be negative");
    }

    @Test
    void translatesInvalidProductUpdateToPublishedCatalogFailure() {
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.of(new VersionedProduct(existingProduct(), REVISION.value())));

        assertThatThrownBy(() -> service.updateProduct(PRODUCT, REVISION, updateCommand(true, -1)))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("Stock quantity must not be negative");
        verify(products, never()).updateForAdministration(any(Product.class), any());
    }

    @Test
    void duplicateSkuFailurePassesThrough() {
        DuplicateSkuException failure = new DuplicateSkuException("ORIGINAL-SKU");
        when(products.insertForAdministration(any(Product.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createProduct(createCommand())).isSameAs(failure);
    }

    @Test
    void missingCategoryFailurePassesThrough() {
        CategoryNotFoundException failure =
                new CategoryNotFoundException(new CategoryReference(10L));
        when(products.insertForAdministration(any(Product.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createProduct(createCommand())).isSameAs(failure);
    }

    @Test
    void staleRevisionFailurePassesThrough() {
        StaleProductRevisionException failure =
                new StaleProductRevisionException(PRODUCT, REVISION);
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.of(new VersionedProduct(existingProduct(), REVISION.value())));
        when(products.updateForAdministration(any(Product.class), eq(REVISION))).thenThrow(failure);

        assertThatThrownBy(() -> service.updateProduct(PRODUCT, REVISION, updateCommand(true, 11)))
                .isSameAs(failure);
    }

    @Test
    void deactivationLocksAndUpdatesTheAggregateWithoutDeletingIt() {
        Product loaded = existingProduct();
        when(products.findForAdministrationUpdate(new ProductId(PRODUCT.value())))
                .thenReturn(Optional.of(new VersionedProduct(loaded, REVISION.value())));
        when(products.updateForAdministration(loaded, REVISION))
                .thenReturn(new VersionedProduct(loaded, REVISION.value() + 1));

        service.deactivateProduct(PRODUCT, REVISION);

        assertThat(loaded.active()).isFalse();
        verify(products).findForAdministrationUpdate(new ProductId(PRODUCT.value()));
        verify(products).updateForAdministration(loaded, REVISION);
    }

    private CreateProductCommand createCommand() {
        return new CreateProductCommand(
                "ORIGINAL-SKU",
                "Widget",
                "Useful widget",
                money("19.99"),
                7,
                "https://example.com/widget.png",
                Set.of(new CategoryReference(10L)));
    }

    private UpdateProductCommand updateCommand(boolean active, int stockQuantity) {
        return new UpdateProductCommand(
                "Updated widget",
                "Updated description",
                money("29.99"),
                stockQuantity,
                "https://example.com/updated.png",
                active,
                Set.of(new CategoryReference(12L)));
    }

    private Product existingProduct() {
        return Product.restore(
                new ProductId(PRODUCT.value()),
                "Widget",
                "Useful widget",
                true,
                Set.of(new CategoryId(10L)),
                List.of(
                        ProductVariant.restore(
                                new ProductVariantId(1001L),
                                new Sku("ORIGINAL-SKU"),
                                money("19.99"),
                                7,
                                "https://example.com/widget.png",
                                true,
                                true)));
    }

    private Product productFamily() {
        Product product = existingProduct();
        product.addVariant(
                ProductVariant.restore(
                        new ProductVariantId(1002L),
                        new Sku("SECOND-SKU"),
                        money("24.99"),
                        3,
                        null,
                        true,
                        false));
        return product;
    }

    private Category category(long id, String name, String slug) {
        return Category.restore(new CategoryId(id), name, slug);
    }

    private Money money(String amount) {
        return new Money(new BigDecimal(amount));
    }
}
