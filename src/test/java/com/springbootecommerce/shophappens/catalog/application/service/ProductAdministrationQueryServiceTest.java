package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductAdministrationQueryServiceTest {

    @Test
    void findsAProductDirectlyByIdRegardlessOfItsSearchPage() {
        ProductRepository products = mock(ProductRepository.class);
        ProductAdminView expected = mock(ProductAdminView.class);
        when(products.findAdminViewById(new ProductId(101L))).thenReturn(Optional.of(expected));

        ProductAdministrationQueryService service = new ProductAdministrationQueryService(products);

        assertThat(service.findProduct(new ProductReference(101L))).containsSame(expected);
        verify(products).findAdminViewById(new ProductId(101L));
    }

    @Test
    void returnsEmptyForInvalidProductReferences() {
        ProductRepository products = mock(ProductRepository.class);
        ProductAdministrationQueryService service = new ProductAdministrationQueryService(products);

        assertThat(service.findProduct(new ProductReference(0L))).isEmpty();
    }
}
