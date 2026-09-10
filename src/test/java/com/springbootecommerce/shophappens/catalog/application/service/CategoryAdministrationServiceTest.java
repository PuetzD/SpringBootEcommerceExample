package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCategoryException;
import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import com.springbootecommerce.shophappens.catalog.application.port.in.RenameCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleCategoryRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedCategory;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CategoryAdministrationServiceTest {
    private static final CategoryReference CATEGORY = new CategoryReference(7L);
    private static final CategoryId CATEGORY_ID = new CategoryId(CATEGORY.value());

    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final CategoryAdministrationService service =
            new CategoryAdministrationService(categories);

    @Test
    void createsCategoryThroughTheAggregateAndReturnsTheStoredRevisionAndMembershipCount() {
        when(categories.insertForAdministration(any(Category.class)))
                .thenReturn(new VersionedCategory(existingCategory(), 0L));
        when(categories.countProductsForAdministration(CATEGORY_ID)).thenReturn(2L);

        CategoryAdminView result =
                service.createCategory(new CreateCategoryCommand(" Desk Tools "));

        verify(categories)
                .insertForAdministration(
                        argThat(
                                category ->
                                        category.name().equals("Desk Tools")
                                                && category.slug().equals("desk-tools")));
        assertThat(result)
                .isEqualTo(
                        new CategoryAdminView(
                                CATEGORY,
                                "Desk Tools",
                                "desk-tools",
                                new CategoryRevision(0L),
                                2L));
    }

    @Test
    void translatesInvalidCategoryCreationToAPublishedCatalogFailure() {
        assertThatThrownBy(() -> service.createCategory(new CreateCategoryCommand(" ")))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("Name must not be blank");

        verify(categories, never()).insertForAdministration(any());
    }

    @Test
    void duplicateCategoryCreationFailurePassesThroughUnchanged() {
        DuplicateCategoryException failure =
                new DuplicateCategoryException("Desk Tools", "desk-tools");
        when(categories.insertForAdministration(any(Category.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createCategory(new CreateCategoryCommand("Desk Tools")))
                .isSameAs(failure);
    }

    @Test
    void repositoryIllegalArgumentFailureDuringCreatePassesThroughUnchanged() {
        IllegalArgumentException failure =
                new IllegalArgumentException("persistence mapping failed");
        when(categories.insertForAdministration(any(Category.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createCategory(new CreateCategoryCommand("Desk Tools")))
                .isSameAs(failure);
    }

    @Test
    void renamesThroughTheAggregateUsingTheSuppliedRevision() {
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.updateForAdministration(any(Category.class), any(CategoryRevision.class)))
                .thenAnswer(
                        invocation ->
                                new VersionedCategory(
                                        invocation.getArgument(0, Category.class), 3L));
        when(categories.countProductsForAdministration(CATEGORY_ID)).thenReturn(4L);

        CategoryAdminView result =
                service.renameCategory(
                        CATEGORY,
                        new CategoryRevision(2L),
                        new RenameCategoryCommand("Office Supplies"));

        verify(categories)
                .updateForAdministration(
                        argThat(
                                category ->
                                        category.name().equals("Office Supplies")
                                                && category.slug().equals("office-supplies")),
                        eq(new CategoryRevision(2L)));
        assertThat(result.revision()).isEqualTo(new CategoryRevision(3L));
        assertThat(result.productCount()).isEqualTo(4L);
    }

    @Test
    void translatesInvalidCategoryRenameToAPublishedCatalogFailure() {
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(2L),
                                        new RenameCategoryCommand(" ")))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("Name must not be blank");

        verify(categories, never())
                .updateForAdministration(any(Category.class), any(CategoryRevision.class));
    }

    @Test
    void missingCategoryRenameFailsWithThePublishedNotFoundFailure() {
        when(categories.findForAdministration(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(2L),
                                        new RenameCategoryCommand("Office Supplies")))
                .isInstanceOf(CategoryNotFoundException.class)
                .satisfies(
                        failure ->
                                assertThat(((CategoryNotFoundException) failure).category())
                                        .isEqualTo(CATEGORY));
    }

    @Test
    void repositoryNotFoundFailureDuringRenamePassesThroughUnchanged() {
        CategoryNotFoundException failure = new CategoryNotFoundException(CATEGORY);
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.updateForAdministration(any(Category.class), any(CategoryRevision.class)))
                .thenThrow(failure);

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(2L),
                                        new RenameCategoryCommand("Office Supplies")))
                .isSameAs(failure);
    }

    @Test
    void duplicateRenameFailurePassesThroughUnchanged() {
        DuplicateCategoryException failure =
                new DuplicateCategoryException("Office Supplies", "office-supplies");
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.updateForAdministration(any(Category.class), any(CategoryRevision.class)))
                .thenThrow(failure);

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(2L),
                                        new RenameCategoryCommand("Office Supplies")))
                .isSameAs(failure);
    }

    @Test
    void staleRenameFailurePassesThroughUnchanged() {
        StaleCategoryRevisionException failure =
                new StaleCategoryRevisionException(CATEGORY, new CategoryRevision(1L));
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.updateForAdministration(any(Category.class), any(CategoryRevision.class)))
                .thenThrow(failure);

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(1L),
                                        new RenameCategoryCommand("Office Supplies")))
                .isSameAs(failure);
    }

    @Test
    void repositoryIllegalArgumentFailureDuringRenamePassesThroughUnchanged() {
        IllegalArgumentException failure =
                new IllegalArgumentException("persistence mapping failed");
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.updateForAdministration(any(Category.class), any(CategoryRevision.class)))
                .thenThrow(failure);

        assertThatThrownBy(
                        () ->
                                service.renameCategory(
                                        CATEGORY,
                                        new CategoryRevision(2L),
                                        new RenameCategoryCommand("Office Supplies")))
                .isSameAs(failure);
    }

    @Test
    void rejectsDeletionWhenAnyProductReferencesTheCategory() {
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.isReferencedByAnyProduct(CATEGORY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCategory(CATEGORY, new CategoryRevision(2L)))
                .isInstanceOf(CategoryInUseException.class)
                .satisfies(
                        failure ->
                                assertThat(((CategoryInUseException) failure).category())
                                        .isEqualTo(CATEGORY));

        verify(categories, never()).deleteForAdministration(any(), any());
    }

    @Test
    void missingCategoryDeletionFailsBeforeCheckingMembership() {
        when(categories.findForAdministration(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCategory(CATEGORY, new CategoryRevision(2L)))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categories, never()).isReferencedByAnyProduct(any());
        verify(categories, never()).deleteForAdministration(any(), any());
    }

    @Test
    void deletesAnUnusedCategoryUsingTheSuppliedRevision() {
        CategoryRevision revision = new CategoryRevision(2L);
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.isReferencedByAnyProduct(CATEGORY_ID)).thenReturn(false);

        service.deleteCategory(CATEGORY, revision);

        verify(categories).deleteForAdministration(CATEGORY_ID, revision);
    }

    @Test
    void repositoryInUseFailureDuringDeletePassesThroughUnchanged() {
        CategoryInUseException failure = new CategoryInUseException(CATEGORY);
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.isReferencedByAnyProduct(CATEGORY_ID)).thenReturn(false);
        doThrow(failure)
                .when(categories)
                .deleteForAdministration(CATEGORY_ID, new CategoryRevision(2L));

        assertThatThrownBy(() -> service.deleteCategory(CATEGORY, new CategoryRevision(2L)))
                .isSameAs(failure);
    }

    @Test
    void repositoryNotFoundFailureDuringDeletePassesThroughUnchanged() {
        CategoryNotFoundException failure = new CategoryNotFoundException(CATEGORY);
        CategoryRevision revision = new CategoryRevision(2L);
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.isReferencedByAnyProduct(CATEGORY_ID)).thenReturn(false);
        doThrow(failure).when(categories).deleteForAdministration(CATEGORY_ID, revision);

        assertThatThrownBy(() -> service.deleteCategory(CATEGORY, revision)).isSameAs(failure);
    }

    @Test
    void staleDeleteFailurePassesThroughUnchanged() {
        CategoryRevision revision = new CategoryRevision(1L);
        StaleCategoryRevisionException failure =
                new StaleCategoryRevisionException(CATEGORY, revision);
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(Optional.of(new VersionedCategory(existingCategory(), 2L)));
        when(categories.isReferencedByAnyProduct(CATEGORY_ID)).thenReturn(false);
        doThrow(failure).when(categories).deleteForAdministration(CATEGORY_ID, revision);

        assertThatThrownBy(() -> service.deleteCategory(CATEGORY, revision)).isSameAs(failure);
    }

    private Category existingCategory() {
        return Category.restore(CATEGORY_ID, "Desk Tools", "desk-tools");
    }
}
