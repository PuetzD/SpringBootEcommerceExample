package com.springbootecommerce.shophappens.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void purchasesActiveStockAndReturnsCurrentFacts() {
        Product product = productWithStock(5);

        product.purchase(2);

        assertThat(product.stockQuantity()).isEqualTo(3);
        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("19.99")));
    }

    @Test
    void rejectsInactiveAndInsufficientProductsWithoutChangingStock() {
        Product inactive = productWithStock(5);
        inactive.deactivate();
        assertThatThrownBy(() -> inactive.purchase(1))
                .isInstanceOf(ProductUnavailableException.class);
        assertThat(inactive.stockQuantity()).isEqualTo(5);

        Product scarce = productWithStock(1);
        assertThatThrownBy(() -> scarce.purchase(2)).isInstanceOf(InsufficientStockException.class);
        assertThat(scarce.stockQuantity()).isEqualTo(1);
    }

    @Test
    void deactivationMakesProductsUnavailable() {
        Product product = productWithStock(5);

        product.deactivate();

        assertThat(product.active()).isFalse();
        assertThatThrownBy(() -> product.purchase(1))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void categoryIdsReturnsAnUnmodifiableCopy() {
        Set<CategoryId> source = new HashSet<>(Set.of(new CategoryId(3L)));
        Product product =
                Product.create(
                        new Sku("ELEC-001"),
                        "Headphones",
                        "Description",
                        new Money(new BigDecimal("19.99")),
                        5,
                        "/images/product-placeholder.svg",
                        source);

        source.add(new CategoryId(9L));

        assertThat(product.categoryIds()).containsExactly(new CategoryId(3L));
        assertThatThrownBy(() -> product.categoryIds().add(new CategoryId(7L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void revisesDetailsAndCategoriesWithoutChangingSku() {
        Product product = productWithStock(5);

        product.reviseDetails(
                "  New name  ", "New description", new Money(new BigDecimal("12.50")), "/new.png");
        product.replaceCategories(Set.of(new CategoryId(3L), new CategoryId(4L)));

        assertThat(product.sku()).isEqualTo(new Sku("ELEC-001"));
        assertThat(product.name()).isEqualTo("New name");
        assertThat(product.description()).isEqualTo("New description");
        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("12.50")));
        assertThat(product.imageUrl()).isEqualTo("/new.png");
        assertThat(product.categoryIds())
                .containsExactlyInAnyOrder(new CategoryId(3L), new CategoryId(4L));
    }

    @Test
    void validatesAdministrativeChangesAndActivation() {
        Product product = productWithStock(5);

        assertThatThrownBy(() -> product.reviseDetails(" ", "Description", product.price(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> product.setStockQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class);

        product.deactivate();
        product.activate();

        assertThat(product.active()).isTrue();
    }

    @Test
    void createsOneDefaultVariantFromSimpleProductFields() {
        Product product = productWithStock(5);

        assertThat(product.variants()).hasSize(1);
        assertThat(product.defaultVariant().sku()).isEqualTo(new Sku("ELEC-001"));
        assertThat(product.defaultVariant().price()).isEqualTo(new Money(new BigDecimal("19.99")));
        assertThat(product.defaultVariant().stockQuantity()).isEqualTo(5);
        assertThat(product.defaultVariant().isDefault()).isTrue();
    }

    @Test
    void cannotDeleteTheLastVariant() {
        Product product = productWithStock(5);

        assertThatThrownBy(() -> product.removeVariant(product.defaultVariant()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addingVariantKeepsExactlyOneDefaultVariant() {
        Product product = productWithStock(5);
        ProductVariant additional =
                ProductVariant.create(
                        new Sku("ELEC-002"),
                        new Money(new BigDecimal("24.99")),
                        2,
                        "/blue.png",
                        true,
                        false);

        product.addVariant(additional);

        assertThat(product.variants()).hasSize(2);
        assertThat(product.variants()).filteredOn(ProductVariant::isDefault).hasSize(1);
        assertThat(product.variants())
                .extracting(ProductVariant::sku)
                .containsExactlyInAnyOrder(new Sku("ELEC-001"), new Sku("ELEC-002"));
    }

    @Test
    void familyEditsPreserveWithdrawnVariant() {
        var regular =
                ProductVariant.restore(
                        new ProductVariantId(101),
                        new Sku("TEE-A"),
                        new Money(new BigDecimal("10.00")),
                        5,
                        null,
                        true,
                        true);
        var withdrawn =
                ProductVariant.restore(
                        new ProductVariantId(202),
                        new Sku("TEE-B"),
                        new Money(new BigDecimal("20.00")),
                        8,
                        null,
                        false,
                        false);
        var family =
                Product.restore(
                        new ProductId(11),
                        "Tee",
                        "Cotton",
                        true,
                        Set.of(),
                        List.of(regular, withdrawn));

        family.reviseDetails("Renamed tee", "Cotton", family.price(), null);
        family.activate();

        assertThat(withdrawn.active()).isFalse();

        family.deactivate();

        assertThat(regular.active()).isTrue();
        assertThatThrownBy(() -> family.purchase(new ProductVariantId(101), 1))
                .isInstanceOf(ProductUnavailableException.class);

        family.activate();

        assertThat(withdrawn.active()).isFalse();
        assertThatThrownBy(() -> family.purchase(new ProductVariantId(202), 1))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void defaultAliasIgnoresCollectionOrderAndTracksRename() {
        var regular =
                ProductVariant.restore(
                        new ProductVariantId(101),
                        new Sku("TEE-A"),
                        new Money(new BigDecimal("10.00")),
                        5,
                        null,
                        true,
                        true);
        var sibling =
                ProductVariant.restore(
                        new ProductVariantId(202),
                        new Sku("TEE-B"),
                        new Money(new BigDecimal("20.00")),
                        8,
                        null,
                        true,
                        false);
        var family =
                Product.restore(
                        new ProductId(11),
                        "Tee",
                        "Cotton",
                        true,
                        Set.of(),
                        List.of(sibling, regular));

        assertThat(family.sku()).isEqualTo(new Sku("TEE-A"));

        family.reviseVariant(
                new ProductVariantId(101), new Sku("TEE-NEW"), regular.price(), 5, null, true);

        assertThat(family.sku()).isEqualTo(new Sku("TEE-NEW"));
    }

    private Product productWithStock(int stock) {
        return Product.restore(
                new ProductId(7L),
                "Headphones",
                "Description",
                true,
                Set.of(new CategoryId(3L)),
                List.of(
                        ProductVariant.restore(
                                new ProductVariantId(701L),
                                new Sku("ELEC-001"),
                                new Money(new BigDecimal("19.99")),
                                stock,
                                "/images/product-placeholder.svg",
                                true,
                                true)));
    }
}
