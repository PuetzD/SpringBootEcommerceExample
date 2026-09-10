package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedCategory;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CategoryAdministrationQueryServiceTest {
    private static final CategoryReference CATEGORY = new CategoryReference(7L);
    private static final CategoryId CATEGORY_ID = new CategoryId(CATEGORY.value());

    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final CategoryAdministrationQueryService service =
            new CategoryAdministrationQueryService(categories);

    @Test
    void delegatesTheExactBoundedAdministrationPageSearch() {
        CategoryAdminSearch search = new CategoryAdminSearch(2, 25);
        CategoryAdminPage expected =
                new CategoryAdminPage(
                        List.of(
                                new CategoryAdminView(
                                        CATEGORY,
                                        "Desk Tools",
                                        "desk-tools",
                                        new CategoryRevision(3L),
                                        4L)),
                        2,
                        25,
                        76,
                        4);
        when(categories.searchForAdministration(search)).thenReturn(expected);

        assertThat(service.listCategories(search)).isSameAs(expected);
        verify(categories).searchForAdministration(search);
        verify(categories, never()).findAll();
    }

    @Test
    void mapsDetailFromTheDirectVersionedLookupAndMembershipCount() {
        when(categories.findForAdministration(CATEGORY_ID))
                .thenReturn(
                        Optional.of(
                                new VersionedCategory(
                                        Category.restore(CATEGORY_ID, "Desk Tools", "desk-tools"),
                                        3L)));
        when(categories.countProductsForAdministration(CATEGORY_ID)).thenReturn(4L);

        assertThat(service.findCategory(CATEGORY))
                .contains(
                        new CategoryAdminView(
                                CATEGORY,
                                "Desk Tools",
                                "desk-tools",
                                new CategoryRevision(3L),
                                4L));
        verify(categories).findForAdministration(CATEGORY_ID);
        verify(categories).countProductsForAdministration(CATEGORY_ID);
        verify(categories, never()).findAll();
    }

    @Test
    void returnsEmptyDetailWithoutCountingMembershipForAMissingCategory() {
        when(categories.findForAdministration(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThat(service.findCategory(CATEGORY)).isEmpty();

        verify(categories).findForAdministration(CATEGORY_ID);
        verify(categories, never()).countProductsForAdministration(CATEGORY_ID);
    }

    @Test
    void delegatesOrderedAdministrationOptionsWithoutLoadingAllCategories() {
        List<CategoryOption> expected =
                List.of(new CategoryOption(CATEGORY, "Desk Tools", "desk-tools"));
        when(categories.findOptionsForAdministration()).thenReturn(expected);

        assertThat(service.listCategoryOptions()).isSameAs(expected);
        verify(categories).findOptionsForAdministration();
        verify(categories, never()).findAll();
    }
}
