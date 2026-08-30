package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {
    @Mock CategoryRepository categoryRepository;
    @Mock ProductRepository productRepository;
    CategoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryQueryService(categoryRepository, productRepository);
    }

    @Test
    void findAllActiveReturnsAllCategoriesMappedToSummaries() {
        Category cat1 = restoredCategory(1L, "Electronics");
        Category cat2 = restoredCategory(2L, "Books");
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        Product prod1 = restoredProduct(1L, "ELEC-001", "Laptop", "999.99", 5, Set.of(new CategoryId(1L)));
        Product prod2 = restoredProduct(2L, "BOOK-001", "Novel", "19.99", 10, Set.of(new CategoryId(2L)));
        when(productRepository.findAllActive()).thenReturn(List.of(prod1, prod2));

        var result = service.findAllActive();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(new CategoryId(1L));
        assertThat(result.get(0).name()).isEqualTo("Electronics");
        assertThat(result.get(0).slug()).isEqualTo("electronics");
        assertThat(result.get(0).productCount()).isEqualTo(1);
        assertThat(result.get(1).id()).isEqualTo(new CategoryId(2L));
        assertThat(result.get(1).name()).isEqualTo("Books");
        assertThat(result.get(1).slug()).isEqualTo("books");
        assertThat(result.get(1).productCount()).isEqualTo(1);
    }

    @Test
    void findBySlugReturnsEmptyForUnknownSlug() {
        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        var result = service.findBySlug("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findBySlugReturnsSummaryForKnownSlug() {
        Category cat = restoredCategory(1L, "Electronics");
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(cat));
        Product prod1 = restoredProduct(1L, "ELEC-001", "Laptop", "999.99", 5, Set.of(new CategoryId(1L)));
        when(productRepository.findAllActive()).thenReturn(List.of(prod1));

        var result = service.findBySlug("electronics");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(new CategoryId(1L));
        assertThat(result.get().name()).isEqualTo("Electronics");
        assertThat(result.get().slug()).isEqualTo("electronics");
        assertThat(result.get().productCount()).isEqualTo(1);
    }

    @Test
    void findActiveProductsByCategorySlugReturnsProductsForKnownCategory() {
        Category cat = restoredCategory(1L, "Electronics");
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(cat));
        Product prod1 = restoredProduct(1L, "ELEC-001", "Laptop", "999.99", 5, Set.of(new CategoryId(1L)));
        Product prod2 = restoredProduct(2L, "BOOK-001", "Novel", "19.99", 10, Set.of(new CategoryId(2L)));
        when(productRepository.findAllActive()).thenReturn(List.of(prod1, prod2));

        var result = service.findActiveProductsByCategorySlug("electronics");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).product().value()).isEqualTo(1L);
        assertThat(result.get(0).sku()).isEqualTo("ELEC-001");
        assertThat(result.get(0).name()).isEqualTo("Laptop");
    }

    @Test
    void findActiveProductsByCategorySlugReturnsEmptyListForUnknownCategory() {
        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findActiveProductsByCategorySlug("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category not found: unknown");
    }

    private Category restoredCategory(long id, String name) {
        return Category.restore(new CategoryId(id), name);
    }

    private Product restoredProduct(
            long id, String sku, String name, String price, int stock, Set<CategoryId> categoryIds) {
        return Product.restore(
                new ProductId(id),
                new Sku(sku),
                name,
                "Description",
                new Money(new BigDecimal(price)),
                stock,
                "/images/product-placeholder.svg",
                true,
                categoryIds);
    }
}
