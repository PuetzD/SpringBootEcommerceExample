package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.application.port.in.AmbiguousProductUpdateException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductFamilyCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductVariantCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CatalogIntegrityIT extends AbstractIntegrationTest {
    @Autowired ProductRepository products;
    @Autowired ProductAdministrationUseCase admin;
    @Autowired JdbcTemplate jdbc;

    @Test
    void renamedDefaultIsTheOnlyCompatibilitySkuAndLegacyStorageStaysFrozen() {
        Product product = family("ALIAS");
        var id = product.id().orElseThrow();
        var loaded = products.findForAdministration(id).orElseThrow();

        var revised =
                admin.updateVariant(
                        new ProductReference(id.value()),
                        product.defaultVariant().id().orElseThrow(),
                        new ProductRevision(loaded.revision()),
                        new UpdateProductVariantCommand(
                                "NEW-ALIAS",
                                new Money(new BigDecimal("13.00")),
                                4,
                                "/images/new.svg",
                                true));

        assertThat(revised.productRevision().value()).isGreaterThan(loaded.revision());
        assertThat(products.findActiveBySku(new Sku("NEW-ALIAS"))).isPresent();
        assertThat(products.findActiveBySku(new Sku("BASE-ALIAS"))).isEmpty();
        assertThat(products.findActiveBySku(new Sku("ALT-ALIAS"))).isEmpty();
        assertThat(products.findById(id).orElseThrow().sku()).isEqualTo(new Sku("NEW-ALIAS"));
        assertThat(
                        products.searchForAdministration(
                                        new ProductAdminSearch(0, 20, "NEW-ALIAS", null))
                                .content())
                .extracting(view -> view.product().value())
                .contains(id.value());
        assertThat(
                        products.searchForAdministration(
                                        new ProductAdminSearch(0, 20, "ALT-ALIAS", null))
                                .content())
                .extracting(view -> view.product().value())
                .contains(id.value());
        assertThat(
                        jdbc.queryForObject(
                                "select sku from product where id = ?", String.class, id.value()))
                .isEqualTo("BASE-ALIAS");
        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product where id = ?",
                                Integer.class,
                                id.value()))
                .isEqualTo(5);
        assertThat(
                        jdbc.queryForObject(
                                "select price from product where id = ?",
                                BigDecimal.class,
                                id.value()))
                .isEqualByComparingTo("10.00");
        assertThat(
                        jdbc.queryForObject(
                                "select image_url from product where id = ?",
                                String.class,
                                id.value()))
                .isNull();
    }

    @Test
    void releasesLegacySkuAfterRenamingDefaultVariant() {
        Product original = family("RELEASE");
        var originalId = original.id().orElseThrow();
        var loaded = products.findForAdministration(originalId).orElseThrow();

        var revised =
                admin.updateVariant(
                        new ProductReference(originalId.value()),
                        original.defaultVariant().id().orElseThrow(),
                        new ProductRevision(loaded.revision()),
                        new UpdateProductVariantCommand(
                                "RELEASED-SKU", new Money(new BigDecimal("13.00")), 4, null, true));

        var replacement =
                admin.createProduct(
                        new CreateProductCommand(
                                "BASE-RELEASE",
                                "Replacement",
                                "Replacement",
                                new Money(new BigDecimal("11.00")),
                                2,
                                null,
                                Set.of()));

        assertThat(revised.productRevision().value()).isGreaterThan(loaded.revision());
        assertThat(replacement.sku()).isEqualTo("BASE-RELEASE");
        assertThat(products.findActiveBySku(new Sku("RELEASED-SKU"))).isPresent();
    }

    @Test
    void withdrawnDefaultRemainsANavigationAliasButNotAnActiveSelection() {
        Product product = family("WITHDRAWN");
        var id = product.id().orElseThrow();
        var defaultVariant = product.defaultVariant().id().orElseThrow();
        var loaded = products.findForAdministration(id).orElseThrow();

        var revised =
                admin.updateVariant(
                        new ProductReference(id.value()),
                        defaultVariant,
                        new ProductRevision(loaded.revision()),
                        new UpdateProductVariantCommand(
                                "BASE-WITHDRAWN",
                                new Money(new BigDecimal("10.00")),
                                5,
                                null,
                                false));

        assertThat(products.findActiveBySku(new Sku("BASE-WITHDRAWN"))).isPresent();
        assertThat(products.findActiveByVariantId(defaultVariant)).isEmpty();

        admin.deactivateProduct(new ProductReference(id.value()), revised.productRevision());

        assertThat(products.findActiveBySku(new Sku("BASE-WITHDRAWN"))).isEmpty();
    }

    @Test
    void descriptiveFamilyEditWorksWhileLegacyCommercialEditConflicts() {
        var original = family("EDIT");
        var id = original.id().orElseThrow();
        var revision =
                new ProductRevision(products.findForAdministration(id).orElseThrow().revision());
        var reference = new ProductReference(id.value());

        assertThatThrownBy(
                        () ->
                                admin.updateProduct(
                                        reference,
                                        revision,
                                        new UpdateProductCommand(
                                                "Wrong",
                                                "Changed",
                                                new Money(new BigDecimal("99.00")),
                                                99,
                                                null,
                                                true,
                                                Set.of())))
                .isInstanceOf(AmbiguousProductUpdateException.class);
        assertThat(products.findById(id).orElseThrow().name()).isEqualTo("Tee EDIT");

        var saved =
                admin.updateProductFamily(
                        reference,
                        revision,
                        new UpdateProductFamilyCommand("Renamed", "Soft cotton", true, Set.of()));

        assertThat(saved.name()).isEqualTo("Renamed");
        var reloaded = products.findById(id).orElseThrow();
        assertThat(reloaded.defaultVariant().stockQuantity()).isEqualTo(5);
        assertThat(
                        reloaded.variants().stream()
                                .filter(variant -> !variant.isDefault())
                                .findFirst()
                                .orElseThrow()
                                .active())
                .isFalse();
        assertThatThrownBy(
                        () ->
                                admin.updateProduct(
                                        reference,
                                        revision,
                                        new UpdateProductCommand(
                                                "Stale legacy",
                                                "Old",
                                                new Money(new BigDecimal("99.00")),
                                                99,
                                                null,
                                                false,
                                                Set.of())))
                .isInstanceOf(StaleProductRevisionException.class);
        assertThatThrownBy(
                        () ->
                                admin.updateProductFamily(
                                        reference,
                                        revision,
                                        new UpdateProductFamilyCommand(
                                                "Stale", "Old", false, Set.of())))
                .isInstanceOf(StaleProductRevisionException.class);
    }

    private Product family(String suffix) {
        Product product =
                Product.create(
                        new Sku("BASE-" + suffix),
                        "Tee " + suffix,
                        "Cotton",
                        new Money(new BigDecimal("10.00")),
                        5,
                        null,
                        Set.of());
        product.addVariant(
                ProductVariant.create(
                        new Sku("ALT-" + suffix),
                        new Money(new BigDecimal("20.00")),
                        8,
                        null,
                        false,
                        false));
        return products.save(product);
    }
}
