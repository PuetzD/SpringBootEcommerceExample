package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCategoryException;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleCategoryRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedCategory;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class CategoryRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clearCatalogFixtures() {
        jdbc.update("delete from product_category");
        jdbc.update("delete from product_variant");
        jdbc.update("delete from product");
        jdbc.update("delete from category");
    }

    @Test
    void ordersCategoriesByNameThenId() {
        insertCategory("Zebras");
        insertCategory("Apples");
        insertCategory("Mangoes");

        List<Category> results = categories.findAll();

        assertThat(results)
                .extracting(Category::name)
                .containsExactly("Apples", "Mangoes", "Zebras");
    }

    @Test
    void pagesCategoriesInDatabaseNameOrderWithinRequestedSize() {
        CategoryId alpha = insertCategory("!! Task 8 page Alpha");
        CategoryId bravo = insertCategory("!! Task 8 page Bravo");
        insertCategory("!! Task 8 page Charlie");

        var page = categories.searchForAdministration(new CategoryAdminSearch(0, 2));

        assertThat(page.content())
                .extracting(view -> view.category().value())
                .containsExactly(alpha.value(), bravo.value());
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.totalPages()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void countsActiveAndInactiveProductMembershipsInAdministrationViews() {
        CategoryId category = insertCategory("Task 8 membership count");
        insertProductReferencing(category, "TASK8-COUNT-ACTIVE");
        ProductId inactive = insertProductReferencing(category, "TASK8-COUNT-INACTIVE");
        deactivateProductReferencing(inactive);

        var page = categories.searchForAdministration(new CategoryAdminSearch(0, 100));

        assertThat(page.content())
                .filteredOn(view -> view.category().value() == category.value())
                .singleElement()
                .satisfies(view -> assertThat(view.productCount()).isEqualTo(2));
        assertThat(categories.countProductsForAdministration(category)).isEqualTo(2);
    }

    @Test
    void ordersAdministrationOptionsByNameThenId() {
        CategoryId charlie = insertCategory("## Task 8 option Charlie");
        CategoryId alpha = insertCategory("## Task 8 option Alpha");
        CategoryId bravo = insertCategory("## Task 8 option Bravo");
        Set<Long> optionIds = Set.of(alpha.value(), bravo.value(), charlie.value());

        var options = categories.findOptionsForAdministration();

        assertThat(options)
                .filteredOn(option -> optionIds.contains(option.category().value()))
                .extracting(option -> option.category().value())
                .containsExactly(alpha.value(), bravo.value(), charlie.value());
    }

    @Test
    void insertsAggregateGeneratedSlugAtRevisionZero() {
        Category category = Category.create("  Task 8 Summer   Shoes  ");

        VersionedCategory inserted = categories.insertForAdministration(category);

        long id = inserted.category().id().orElseThrow().value();
        assertThat(inserted.revision()).isZero();
        assertThat(inserted.category().name()).isEqualTo("Task 8 Summer   Shoes");
        assertThat(inserted.category().slug()).isEqualTo("task-8-summer-shoes");
        assertThat(jdbc.queryForObject("select slug from category where id = ?", String.class, id))
                .isEqualTo("task-8-summer-shoes");
    }

    @Test
    void renamesAtCurrentRevisionAndIncrementsRevision() {
        VersionedCategory inserted =
                categories.insertForAdministration(Category.create("Task 8 old name"));
        inserted.category().rename("Task 8 current name");

        VersionedCategory updated =
                categories.updateForAdministration(
                        inserted.category(), new CategoryRevision(inserted.revision()));

        assertThat(updated.revision()).isEqualTo(inserted.revision() + 1);
        assertThat(updated.category().name()).isEqualTo("Task 8 current name");
        assertThat(updated.category().slug()).isEqualTo("task-8-current-name");
    }

    @Test
    void rejectsStaleRenameWithoutChangingCurrentData() {
        VersionedCategory inserted =
                categories.insertForAdministration(Category.create("Task 8 rename original"));
        VersionedCategory stale = findCategory(inserted.category().id().orElseThrow());
        VersionedCategory current = findCategory(inserted.category().id().orElseThrow());
        current.category().rename("Task 8 rename current");
        categories.updateForAdministration(
                current.category(), new CategoryRevision(current.revision()));
        stale.category().rename("Task 8 rename stale");

        assertThatThrownBy(
                        () ->
                                categories.updateForAdministration(
                                        stale.category(), new CategoryRevision(stale.revision())))
                .isInstanceOf(StaleCategoryRevisionException.class);
        VersionedCategory reloaded = findCategory(inserted.category().id().orElseThrow());
        assertThat(reloaded.category().name()).isEqualTo("Task 8 rename current");
        assertThat(reloaded.category().slug()).isEqualTo("task-8-rename-current");
        assertThat(reloaded.revision()).isEqualTo(1);
    }

    @Test
    void rejectsStaleDeleteWithoutRemovingCurrentData() {
        VersionedCategory inserted =
                categories.insertForAdministration(Category.create("Task 8 stale delete"));
        inserted.category().rename("Task 8 stale delete current");
        VersionedCategory current =
                categories.updateForAdministration(
                        inserted.category(), new CategoryRevision(inserted.revision()));
        CategoryId id = current.category().id().orElseThrow();

        assertThatThrownBy(
                        () ->
                                categories.deleteForAdministration(
                                        id, new CategoryRevision(inserted.revision())))
                .isInstanceOf(StaleCategoryRevisionException.class);
        VersionedCategory reloaded = findCategory(id);
        assertThat(reloaded.category().name()).isEqualTo("Task 8 stale delete current");
        assertThat(reloaded.category().slug()).isEqualTo("task-8-stale-delete-current");
        assertThat(reloaded.revision()).isEqualTo(1);
    }

    @Test
    void translatesDuplicateNameOnInsert() {
        categories.insertForAdministration(Category.create("Task 8 duplicate name"));

        assertThatThrownBy(
                        () ->
                                categories.insertForAdministration(
                                        Category.create("Task 8 duplicate name")))
                .isInstanceOf(DuplicateCategoryException.class);
    }

    @Test
    void translatesDuplicateSlugOnRename() {
        categories.insertForAdministration(Category.create("Task 8 duplicate slug"));
        VersionedCategory renamed =
                categories.insertForAdministration(Category.create("Task 8 slug rename source"));
        renamed.category().rename("Task 8  duplicate  slug");

        assertThatThrownBy(
                        () ->
                                categories.updateForAdministration(
                                        renamed.category(),
                                        new CategoryRevision(renamed.revision())))
                .isInstanceOf(DuplicateCategoryException.class);
        assertThat(findCategory(renamed.category().id().orElseThrow()).category().name())
                .isEqualTo("Task 8 slug rename source");
    }

    @Test
    void deletesUnusedCategoryAtCurrentRevision() {
        VersionedCategory unused =
                categories.insertForAdministration(Category.create("Task 8 unused category"));
        CategoryId id = unused.category().id().orElseThrow();

        categories.deleteForAdministration(id, new CategoryRevision(unused.revision()));

        assertThat(categories.findForAdministration(id)).isEmpty();
    }

    @Test
    void blocksDeletionForActiveProductMembership() {
        VersionedCategory referenced =
                categories.insertForAdministration(Category.create("Task 8 active membership"));
        CategoryId categoryId = referenced.category().id().orElseThrow();
        insertProductReferencing(categoryId, "TASK8-ACTIVE-MEMBER");

        assertThat(categories.isReferencedByAnyProduct(categoryId)).isTrue();
        assertThatThrownBy(
                        () ->
                                categories.deleteForAdministration(
                                        categoryId, new CategoryRevision(referenced.revision())))
                .isInstanceOf(CategoryInUseException.class);
        assertThat(categories.findForAdministration(categoryId)).isPresent();
    }

    @Test
    void blocksDeletionForInactiveProductMembership() {
        VersionedCategory referenced =
                categories.insertForAdministration(Category.create("Task 8 inactive membership"));
        CategoryId categoryId = referenced.category().id().orElseThrow();
        ProductId productId = insertProductReferencing(categoryId, "TASK8-INACTIVE-MEMBER");
        deactivateProductReferencing(productId);

        assertThat(categories.isReferencedByAnyProduct(categoryId)).isTrue();
        assertThatThrownBy(
                        () ->
                                categories.deleteForAdministration(
                                        categoryId, new CategoryRevision(referenced.revision())))
                .isInstanceOf(CategoryInUseException.class);
        assertThat(categories.findForAdministration(categoryId)).isPresent();
    }

    @Test
    void databaseRejectsDirectCategoryDeleteWhileMembershipExists() {
        CategoryId categoryId = insertCategory("Task 8 direct delete membership");
        insertProductReferencing(categoryId, "TASK8-DIRECT-DELETE");

        assertThatThrownBy(
                        () -> jdbc.update("delete from category where id = ?", categoryId.value()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(categories.findForAdministration(categoryId)).isPresent();
    }

    private VersionedCategory findCategory(CategoryId id) {
        return categories.findForAdministration(id).orElseThrow();
    }

    private CategoryId insertCategory(String name) {
        Long id =
                jdbc.queryForObject(
                        "insert into category (name, slug) values (?, ?) returning id",
                        Long.class,
                        name,
                        name.toLowerCase().replace(' ', '-'));
        return new CategoryId(id);
    }

    private ProductId insertProductReferencing(CategoryId categoryId, String sku) {
        Product product =
                Product.create(
                        new Sku(sku),
                        "Product " + sku,
                        "Task 8 category membership fixture",
                        new Money(new BigDecimal("10.00")),
                        3,
                        "/images/product-placeholder.svg",
                        Set.of(categoryId));
        return products.insertForAdministration(product).product().id().orElseThrow();
    }

    private void deactivateProductReferencing(ProductId productId) {
        jdbc.update("update product set active = false where id = ?", productId.value());
    }
}
