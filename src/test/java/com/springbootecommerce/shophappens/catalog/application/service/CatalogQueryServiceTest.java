package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductPage;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
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
class CatalogQueryServiceTest {
    @Mock ProductRepository productRepository;
    CatalogQueryService service;

    @BeforeEach
    void setUp() {
        service = new CatalogQueryService(productRepository);
    }

    @Test
    void findAllActiveMapsProductsToSummaries() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        Product eight =
                restoredProduct(8L, "MAGI-006", "Staff of Dependency Injection", "89.99", 5);
        when(productRepository.findAllActive()).thenReturn(List.of(seven, eight));

        List<ProductSummary> result = service.findAllActive();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).product().value()).isEqualTo(7L);
        assertThat(result.get(0).sku()).isEqualTo("WEAP-002");
        assertThat(result.get(0).name()).isEqualTo("Rubber Duck of Debugging");
        assertThat(result.get(0).description()).isEqualTo("Description");
        assertThat(result.get(0).price()).isEqualTo(new Money(new BigDecimal("18.99")));
        assertThat(result.get(0).stockQuantity()).isEqualTo(5);
        assertThat(result.get(0).imageUrl()).isEqualTo("/images/product-placeholder.svg");
        assertThat(result.get(1).product().value()).isEqualTo(8L);
        assertThat(result.get(1).sku()).isEqualTo("MAGI-006");
    }

    @Test
    void findActivePageMapsProductsAndMetadata() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        when(productRepository.findActivePage(1, 20))
                .thenReturn(new ProductPage(List.of(seven), 1, 20, 21, 2));

        var result = service.findActivePage(1, 20);

        assertThat(result.products())
                .singleElement()
                .extracting(ProductSummary::sku)
                .isEqualTo("WEAP-002");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(21);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void findActivePageRejectsOversizedPage() {
        assertThatThrownBy(() -> service.findActivePage(0, 21))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("20");
    }

    @Test
    void findActiveByIdReturnsEmptyWhenNotActive() {
        when(productRepository.findActiveById(new ProductId(7L))).thenReturn(Optional.empty());

        Optional<ProductSummary> result = service.findActiveById(new ProductReference(7L));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveByIdReturnsSummaryWhenActive() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        when(productRepository.findActiveById(new ProductId(7L))).thenReturn(Optional.of(seven));

        Optional<ProductSummary> result = service.findActiveById(new ProductReference(7L));

        assertThat(result).isPresent();
        assertThat(result.get().product().value()).isEqualTo(7L);
        assertThat(result.get().sku()).isEqualTo("WEAP-002");
    }

    @Test
    void findActiveBySkuReturnsSummaryWithRightSku() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        when(productRepository.findActiveBySku(new Sku("WEAP-002"))).thenReturn(Optional.of(seven));

        Optional<ProductSummary> result = service.findActiveBySku("WEAP-002");

        assertThat(result).isPresent();
        assertThat(result.get().product().value()).isEqualTo(7L);
        assertThat(result.get().sku()).isEqualTo("WEAP-002");
    }

    @Test
    void findActiveBySkuReturnsEmptyWhenAbsent() {
        when(productRepository.findActiveBySku(new Sku("ELEC-XXX"))).thenReturn(Optional.empty());

        Optional<ProductSummary> result = service.findActiveBySku("ELEC-XXX");

        assertThat(result).isEmpty();
    }

    private Product restoredProduct(long id, String sku, String name, String price, int stock) {
        return Product.restore(
                new ProductId(id),
                new Sku(sku),
                name,
                "Description",
                new Money(new BigDecimal(price)),
                stock,
                "/images/product-placeholder.svg",
                true,
                Set.of());
    }
}
