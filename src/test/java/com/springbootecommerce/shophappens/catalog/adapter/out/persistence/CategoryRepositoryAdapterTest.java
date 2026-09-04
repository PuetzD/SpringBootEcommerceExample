package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CategoryRepositoryAdapterTest {
    private final SpringDataCategoryRepository springData =
            mock(SpringDataCategoryRepository.class);
    private final SpringDataProductRepository products = mock(SpringDataProductRepository.class);
    private final CategoryRepositoryAdapter adapter =
            new CategoryRepositoryAdapter(springData, products);

    @Test
    void updateThrowsCategoryNotFoundWhenCategoryWasDeleted() {
        when(springData.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                adapter.updateForAdministration(
                                        Category.restore(new CategoryId(7L), "Tools", "tools"),
                                        new CategoryRevision(0)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteThrowsCategoryNotFoundWhenCategoryWasDeleted() {
        when(springData.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                adapter.deleteForAdministration(
                                        new CategoryId(7L), new CategoryRevision(0)))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
