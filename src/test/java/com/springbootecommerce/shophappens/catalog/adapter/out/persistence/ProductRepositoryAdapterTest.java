package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class ProductRepositoryAdapterTest {
    private final SpringDataProductRepository springData = mock(SpringDataProductRepository.class);
    private final SpringDataCategoryRepository categories =
            mock(SpringDataCategoryRepository.class);
    private final CatalogPersistenceMapper mapper = mock(CatalogPersistenceMapper.class);
    private final ProductRepositoryAdapter adapter =
            new ProductRepositoryAdapter(springData, categories, mapper);

    @Test
    void searchSkipsProductsDeletedBeforeDetailedQuery() {
        ProductJpaEntity pageProduct =
                ProductJpaEntity.create(
                        "SKU-1", "Widget", "Description", BigDecimal.TEN, 1, null, true);
        pageProduct.setId(1L);
        when(springData.searchForAdministration(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(pageProduct)));
        when(springData.findDetailedByIdIn(List.of(1L))).thenReturn(List.of());

        var result =
                adapter.searchForAdministration(
                        new com.springbootecommerce.shophappens.catalog.application.port.in
                                .ProductAdminSearch(0, 20, null, null));

        assertThat(result.content()).isEmpty();
    }

    @Test
    void updateThrowsProductNotFoundWhenProductWasDeleted() {
        when(springData.findDetailedById(1L)).thenReturn(Optional.empty());
        Product product =
                Product.restore(
                        new ProductId(1L),
                        new Sku("SKU-1"),
                        "Widget",
                        "Description",
                        new Money(BigDecimal.TEN),
                        1,
                        null,
                        true,
                        java.util.Set.of());

        assertThatThrownBy(() -> adapter.updateForAdministration(product, new ProductRevision(0)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
