package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedProduct;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

class ProductRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired ProductRepository products;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void restoresMoneyCategoriesAndOptimisticVersion() {
        seedProduct("FIX-001", "Wireless Headphones", new Money(new BigDecimal("149.99")), 25);

        Product product = products.findActiveBySku(new Sku("FIX-001")).orElseThrow();

        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("149.99")));
        assertThat(product.categoryIds()).isNotEmpty();
        assertThat(product.id()).isPresent();
    }

    @Test
    void persistsAndRestoresOneDefaultVariantForSimpleProduct() {
        Product saved =
                seedProduct(
                        "FIX-VARIANT", "Variant fixture", new Money(new BigDecimal("12.34")), 7);

        Long variantCount =
                jdbc.queryForObject(
                        "select count(*) from product_variant where product_id = ?",
                        Long.class,
                        saved.id().orElseThrow().value());
        Long defaultCount =
                jdbc.queryForObject(
                        "select count(*) from product_variant where product_id = ? and is_default",
                        Long.class,
                        saved.id().orElseThrow().value());
        Product restored = products.findById(saved.id().orElseThrow()).orElseThrow();

        assertThat(variantCount).isEqualTo(1L);
        assertThat(defaultCount).isEqualTo(1L);
        assertThat(restored.variants()).hasSize(1);
        assertThat(restored.defaultVariant().id()).isPresent();
        assertThat(restored.defaultVariant().sku()).isEqualTo(new Sku("FIX-VARIANT"));
    }

    @Test
    void persistsPermanentStockDecrease() {
        seedProduct("FIX-002", "Smart Watch", new Money(new BigDecimal("199.99")), 15);
        Product product = products.findActiveBySku(new Sku("FIX-002")).orElseThrow();
        int before = product.stockQuantity();
        product.purchase(1);

        products.save(product);

        assertThat(products.findById(product.id().orElseThrow()).orElseThrow().stockQuantity())
                .isEqualTo(before - 1);
    }

    @Test
    @Transactional
    void findAllForPurchaseLocksAndRestoresDetailedAggregate() {
        Product seeded =
                seedProduct("FIX-LOCK", "Concurrency Staff", new Money(new BigDecimal("79.99")), 1);
        ProductVariantId variant = seeded.defaultVariant().id().orElseThrow();

        Product product = products.findAllForPurchase(List.of(variant)).getFirst();

        assertThat(product.id()).contains(new ProductId(seeded.id().orElseThrow().value()));
        assertThat(product.categoryIds()).containsExactlyElementsOf(seeded.categoryIds());
        assertThat(product.stockQuantity()).isOne();
    }

    @Test
    void ordersActiveProductsByNameThenId() {
        Product banana =
                Product.create(
                        new Sku("ORD-A"),
                        "Banana",
                        "Yellow fruit",
                        new Money(new BigDecimal("0.50")),
                        20,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product apple =
                Product.create(
                        new Sku("ORD-B"),
                        "Apple",
                        "Red fruit",
                        new Money(new BigDecimal("0.80")),
                        15,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product secondBanana =
                Product.create(
                        new Sku("ORD-C"),
                        "Banana",
                        "Another banana",
                        new Money(new BigDecimal("0.60")),
                        5,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product inactive =
                Product.create(
                        new Sku("ORD-D"),
                        "AAA Inactive",
                        "Should not appear",
                        new Money(new BigDecimal("1.00")),
                        0,
                        "/images/product-placeholder.svg",
                        Set.of());
        inactive.deactivate();
        products.save(banana);
        products.save(apple);
        products.save(secondBanana);
        products.save(inactive);

        List<Product> results = products.findAllActive();

        assertThat(results).isSortedAccordingTo(orderByNameThenId());
        assertThat(
                        results.stream()
                                .map(product -> product.sku().value())
                                .filter(sku -> sku.startsWith("ORD-")))
                .containsExactly("ORD-B", "ORD-A", "ORD-C");
    }

    @Test
    void returnsStableBoundedActivePage() {
        seedProduct("PAGE-A", "Alpha", "1.00", 5);
        seedProduct("PAGE-B", "Bravo", "1.00", 5);
        seedProduct("PAGE-C", "Charlie", "1.00", 5);

        var result = products.findActivePage(1, 2);

        assertThat(result.products()).extracting(Product::name).containsExactly("Charlie");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void searchesAdministrationSkuAndNameCaseInsensitivelyAndFiltersActivity() {
        Product active =
                seedProduct(
                        "ADMIN-SEARCH-SKU",
                        "Case Sensitive Family",
                        new Money(new BigDecimal("10.00")),
                        4);
        Product inactive =
                seedProduct(
                        "ADMIN-INACTIVE-SKU",
                        "Inactive Family",
                        new Money(new BigDecimal("11.00")),
                        3);
        inactive.deactivate();
        products.save(inactive);

        assertThat(
                        products.searchForAdministration(
                                        new ProductAdminSearch(0, 10, "sEaRcH-sKu", null))
                                .content())
                .extracting(view -> view.product().value())
                .containsExactly(active.id().orElseThrow().value());
        assertThat(
                        products.searchForAdministration(
                                        new ProductAdminSearch(0, 10, "sEnSiTiVe fAmIlY", true))
                                .content())
                .extracting(view -> view.product().value())
                .containsExactly(active.id().orElseThrow().value());
        assertThat(
                        products.searchForAdministration(
                                        new ProductAdminSearch(0, 10, "iNaCtIvE fAmIlY", null))
                                .content())
                .extracting(view -> view.product().value())
                .containsExactly(inactive.id().orElseThrow().value());
        assertThat(
                        products.searchForAdministration(new ProductAdminSearch(0, 10, "", true))
                                .content())
                .extracting(view -> view.product().value())
                .contains(active.id().orElseThrow().value())
                .doesNotContain(inactive.id().orElseThrow().value());
        assertThat(
                        products.searchForAdministration(new ProductAdminSearch(0, 10, "", false))
                                .content())
                .extracting(view -> view.product().value())
                .contains(inactive.id().orElseThrow().value())
                .doesNotContain(active.id().orElseThrow().value());
    }

    @Test
    void pagesAdministrationResultsByDescendingIdWithinRequestedSizeAndIncludesCategories() {
        Product first =
                seedProduct(
                        "ADMIN-PAGE-ONE",
                        "Admin page evidence one",
                        new Money(new BigDecimal("10.00")),
                        1);
        Product second =
                seedProduct(
                        "ADMIN-PAGE-TWO",
                        "Admin page evidence two",
                        new Money(new BigDecimal("11.00")),
                        1);
        Product third =
                seedProduct(
                        "ADMIN-PAGE-THREE",
                        "Admin page evidence three",
                        new Money(new BigDecimal("12.00")),
                        1);

        var page =
                products.searchForAdministration(new ProductAdminSearch(0, 2, "ADMIN-PAGE", true));

        assertThat(page.content())
                .extracting(view -> view.product().value())
                .containsExactly(
                        third.id().orElseThrow().value(), second.id().orElseThrow().value());
        assertThat(page.content()).allSatisfy(view -> assertThat(view.categories()).isNotEmpty());
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(first.id().orElseThrow().value())
                .isLessThan(second.id().orElseThrow().value())
                .isLessThan(third.id().orElseThrow().value());
    }

    @Test
    void insertsAdministrationProductAtRevisionZeroWithCategoryMemberships() {
        CategoryId first = createCategory("Admin insert first");
        CategoryId second = createCategory("Admin insert second");
        Product product =
                Product.create(
                        new Sku("ADMIN-INSERT"),
                        "Admin insert family",
                        "Inserted through administration",
                        new Money(new BigDecimal("15.00")),
                        9,
                        "/images/product-placeholder.svg",
                        Set.of(first, second));

        VersionedProduct inserted = products.insertForAdministration(product);

        long id = inserted.product().id().orElseThrow().value();
        assertThat(inserted.revision()).isZero();
        assertThat(inserted.product().categoryIds()).containsExactlyInAnyOrder(first, second);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from product_category where product_id = ?",
                                Long.class,
                                id))
                .isEqualTo(2L);
    }

    @Test
    void updatesCurrentRevisionAndRejectsStaleRevisionWithoutOverwritingNewerPurchase() {
        VersionedProduct inserted =
                products.insertForAdministration(
                        Product.create(
                                new Sku("ADMIN-REVISION"),
                                "Original family",
                                "Original description",
                                new Money(new BigDecimal("15.00")),
                                9,
                                "/images/product-placeholder.svg",
                                Set.of()));
        inserted.product().reviseFamilyDetails("Current family", "Current description");

        VersionedProduct updated =
                products.updateForAdministration(
                        inserted.product(), new ProductRevision(inserted.revision()));
        VersionedProduct stale =
                products.findForAdministration(updated.product().id().orElseThrow()).orElseThrow();
        purchaseInSeparateTransaction(stale.product(), 1);
        stale.product().reviseFamilyDetails("Stale family", "Stale description");

        assertThat(updated.revision()).isGreaterThan(inserted.revision());
        assertThatThrownBy(
                        () ->
                                products.updateForAdministration(
                                        stale.product(), new ProductRevision(stale.revision())))
                .isInstanceOf(StaleProductRevisionException.class);
        Product reloaded = products.findById(updated.product().id().orElseThrow()).orElseThrow();
        assertThat(reloaded.name()).isEqualTo("Current family");
        assertThat(reloaded.stockQuantity()).isEqualTo(8);
    }

    @Test
    void deactivationRetainsTheProductAndCategoryMemberships() {
        CategoryId category = createCategory("Admin deactivation");
        VersionedProduct inserted =
                products.insertForAdministration(
                        Product.create(
                                new Sku("ADMIN-DEACTIVATE"),
                                "Deactivated family",
                                "Retained after deactivation",
                                new Money(new BigDecimal("18.00")),
                                6,
                                "/images/product-placeholder.svg",
                                Set.of(category)));
        inserted.product().deactivate();

        VersionedProduct deactivated =
                products.updateForAdministration(
                        inserted.product(), new ProductRevision(inserted.revision()));
        long id = deactivated.product().id().orElseThrow().value();

        assertThat(deactivated.product().active()).isFalse();
        assertThat(products.findById(new ProductId(id))).isPresent();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from product_category where product_id = ?",
                                Long.class,
                                id))
                .isEqualTo(1L);
    }

    @Test
    void translatesDuplicateVariantSkuOnAdministrationInsert() {
        Product original =
                Product.create(
                        new Sku("ADMIN-DUPLICATE"),
                        "First duplicate family",
                        "First duplicate description",
                        new Money(new BigDecimal("20.00")),
                        2,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product duplicate =
                Product.create(
                        new Sku("ADMIN-DUPLICATE"),
                        "Second duplicate family",
                        "Second duplicate description",
                        new Money(new BigDecimal("21.00")),
                        3,
                        "/images/product-placeholder.svg",
                        Set.of());
        products.insertForAdministration(original);

        assertThatThrownBy(() -> products.insertForAdministration(duplicate))
                .isInstanceOf(DuplicateSkuException.class);
    }

    private static Comparator<Product> orderByNameThenId() {
        return Comparator.comparing(Product::name)
                .thenComparing(product -> product.id().orElseThrow().value());
    }

    private Product seedProduct(String sku, String name, Money price, int stock) {
        CategoryId categoryId = createCategory(name + " category", "cat-" + sku.toLowerCase());
        Product product =
                Product.create(
                        new Sku(sku),
                        name,
                        "Fixture product",
                        price,
                        stock,
                        "/images/product-placeholder.svg",
                        Set.of(categoryId));
        return products.save(product);
    }

    private Product seedProduct(String sku, String name, String price, int stock) {
        return seedProduct(sku, name, new Money(new BigDecimal(price)), stock);
    }

    private CategoryId createCategory(String name) {
        return createCategory(name, "admin-" + name.toLowerCase().replace(' ', '-'));
    }

    private CategoryId createCategory(String name, String slug) {
        Long id =
                jdbc.queryForObject(
                        "insert into category (name, slug) values (?, ?) returning id",
                        Long.class,
                        name,
                        slug);
        return new CategoryId(id);
    }

    private void purchaseInSeparateTransaction(Product product, int quantity) {
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status -> {
                            ProductVariantId variant = product.defaultVariant().id().orElseThrow();
                            Product purchased =
                                    products.findAllForPurchase(List.of(variant)).getFirst();
                            purchased.purchase(variant, quantity);
                            products.save(purchased);
                        });
    }
}
