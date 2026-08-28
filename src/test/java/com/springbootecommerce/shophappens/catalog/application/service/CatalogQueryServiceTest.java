package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductId;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
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
        Product seven = restoredProduct(7L, "ELEC-007", 5);
        Product eight = restoredProduct(8L, "ELEC-008", 5);
        when(productRepository.findAllActive()).thenReturn(List.of(seven, eight));

        List<ProductSummary> result = service.findAllActive();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).product().value()).isEqualTo(7L);
        assertThat(result.get(0).sku()).isEqualTo("ELEC-007");
        assertThat(result.get(0).name()).isEqualTo("Headphones");
        assertThat(result.get(0).description()).isEqualTo("Description");
        assertThat(result.get(0).price()).isEqualTo(new Money(new BigDecimal("19.99")));
        assertThat(result.get(0).stockQuantity()).isEqualTo(5);
        assertThat(result.get(0).imageUrl()).isEqualTo("/images/product-placeholder.svg");
        assertThat(result.get(1).product().value()).isEqualTo(8L);
        assertThat(result.get(1).sku()).isEqualTo("ELEC-008");
    }

    @Test
    void findActiveByIdReturnsEmptyWhenNotActive() {
        when(productRepository.findActiveById(new ProductId(7L))).thenReturn(Optional.empty());

        Optional<ProductSummary> result = service.findActiveById(new ProductReference(7L));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveByIdReturnsSummaryWhenActive() {
        Product seven = restoredProduct(7L, "ELEC-007", 5);
        when(productRepository.findActiveById(new ProductId(7L))).thenReturn(Optional.of(seven));

        Optional<ProductSummary> result = service.findActiveById(new ProductReference(7L));

        assertThat(result).isPresent();
        assertThat(result.get().product().value()).isEqualTo(7L);
        assertThat(result.get().sku()).isEqualTo("ELEC-007");
    }

    @Test
    void findActiveBySkuReturnsSummaryWithRightSku() {
        Product seven = restoredProduct(7L, "ELEC-007", 5);
        when(productRepository.findActiveBySku(new Sku("ELEC-007"))).thenReturn(Optional.of(seven));

        Optional<ProductSummary> result = service.findActiveBySku("ELEC-007");

        assertThat(result).isPresent();
        assertThat(result.get().product().value()).isEqualTo(7L);
        assertThat(result.get().sku()).isEqualTo("ELEC-007");
    }

    @Test
    void findActiveBySkuReturnsEmptyWhenAbsent() {
        when(productRepository.findActiveBySku(new Sku("ELEC-XXX"))).thenReturn(Optional.empty());

        Optional<ProductSummary> result = service.findActiveBySku("ELEC-XXX");

        assertThat(result).isEmpty();
    }

    private Product restoredProduct(long id, String sku, int stock) {
        return Product.restore(
                new ProductId(id),
                new Sku(sku),
                "Headphones",
                "Description",
                new Money(new BigDecimal("19.99")),
                stock,
                "/images/product-placeholder.svg",
                true,
                Set.of());
    }
}
