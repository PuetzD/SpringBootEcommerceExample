package com.springbootecommerce.shophappens.cart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.cart.domain.Cart;
import com.springbootecommerce.shophappens.cart.domain.Quantity;
import com.springbootecommerce.shophappens.cart.persistence.CartRepository;
import com.springbootecommerce.shophappens.catalog.application.CatalogQueryService;
import com.springbootecommerce.shophappens.catalog.application.ProductSummary;
import com.springbootecommerce.shophappens.catalog.domain.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartServiceTest {

    private CartRepository repository;
    private CatalogQueryService catalog;
    private CartService service;

    @BeforeEach
    void setUp() {
        repository = mock(CartRepository.class);
        catalog = mock(CatalogQueryService.class);
        service = new CartService(repository, catalog);
    }

    @Test
    void validatesProductThenAddsItToCustomerCart() {
        when(catalog.findActiveProductById(7L)).thenReturn(Optional.of(product(7L, "Headphones")));
        var cart = Cart.forCustomer(42L);
        when(repository.findByCustomerId(42L)).thenReturn(Optional.of(cart));

        service.addProduct(42L, 7L, 2);

        verify(repository).ensureExistsForCustomer(42L);
        assertThat(cart.items().getFirst().quantity()).isEqualTo(new Quantity(2));
    }

    @Test
    void exposesAContextNeutralCheckoutSelection() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(2));
        when(repository.findByCustomerId(42L)).thenReturn(Optional.of(cart));

        assertThat(service.getSelection(42L))
                .isEqualTo(new CartSelection(42L, List.of(new CartSelectionItem(7L, 2))));
    }

    @Test
    void returnsEmptyDetailsWhenCustomerHasNoCart() {
        when(repository.findByCustomerId(42L)).thenReturn(Optional.empty());

        assertThat(service.getDetails(42L)).isEqualTo(new CartDetails(List.of(), Money.zero()));
    }

    @Test
    void buildsDetailsWithProductLinesAndTotal() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(2));
        when(repository.findByCustomerId(42L)).thenReturn(Optional.of(cart));
        when(catalog.findActiveProductById(7L))
                .thenReturn(Optional.of(product(7L, "Headphones", "99.99")));

        var details = service.getDetails(42L);

        assertThat(details.isEmpty()).isFalse();
        assertThat(details.lines()).hasSize(1);
        assertThat(details.lines().getFirst().sku()).isEqualTo("ELEC-001");
        assertThat(details.lines().getFirst().lineTotal().amount()).isEqualByComparingTo("199.98");
        assertThat(details.total().amount()).isEqualByComparingTo("199.98");
    }

    @Test
    void rejectsMissingProductBeforeCreatingCart() {
        when(catalog.findActiveProductById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addProduct(42L, 99L, 1))
                .isInstanceOf(ProductUnavailableException.class);
        verify(repository, never()).ensureExistsForCustomer(any());
    }

    @Test
    void changesRemovesAndClearsExistingCart() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(1));
        when(repository.findByCustomerId(42L)).thenReturn(Optional.of(cart));

        service.changeQuantity(42L, 7L, 3);
        assertThat(cart.items().getFirst().quantity()).isEqualTo(new Quantity(3));

        service.changeQuantity(42L, 7L, 0);
        assertThat(cart.isEmpty()).isTrue();

        assertThatThrownBy(() -> service.changeQuantity(42L, 7L, -1))
                .isInstanceOf(IllegalArgumentException.class);

        service.clear(42L);
        assertThat(cart.isEmpty()).isTrue();
    }

    private ProductSummary product(Long id, String name) {
        return new ProductSummary(
                id,
                "ELEC-001",
                name,
                "Description for " + name,
                new Money(new BigDecimal("149.99")),
                10,
                "/images/product-placeholder.svg");
    }

    private ProductSummary product(Long id, String name, String price) {
        return new ProductSummary(
                id,
                "ELEC-001",
                name,
                "Description for " + name,
                new Money(new BigDecimal(price)),
                10,
                "/images/product-placeholder.svg");
    }
}
