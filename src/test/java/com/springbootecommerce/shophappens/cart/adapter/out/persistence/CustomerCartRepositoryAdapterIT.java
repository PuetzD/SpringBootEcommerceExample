package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartItem;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerCartRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CustomerCartRepository repository;

    @Test
    void savesOneCartPerCustomerAndRestoresItems() {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Customer(new CustomerId(42L)));
        cart.changeQuantity(new ProductVariantId(701L), new Quantity(2));

        Cart saved = repository.save(cart);
        Cart restored = repository.find(new CustomerId(42L)).orElseThrow();

        assertThat(restored.id()).isEqualTo(saved.id());
        assertThat(restored.items())
                .containsExactly(new CartItem(new ProductVariantId(701L), new Quantity(2)));
    }

    @Test
    void findOrCreateReturnsAStoredCartForOneCustomer() {
        CustomerId customer = new CustomerId(99L);

        Cart first = repository.findOrCreate(customer);
        Cart second = repository.findOrCreate(customer);

        assertThat(first.id()).isEqualTo(second.id());
        assertThat(second.items()).isEmpty();
    }

    @Test
    void clearRemovesTheCustomersCart() {
        CustomerId customer = new CustomerId(77L);
        Cart cart = repository.findOrCreate(customer);
        cart.changeQuantity(new ProductVariantId(701L), new Quantity(1));
        repository.save(cart);

        repository.clear(customer);

        assertThat(repository.find(customer)).isEmpty();
    }

    @Test
    void deletesOnlyTheRequestedCustomerCartAndCanRepeat() {
        CustomerId removed = new CustomerId(77L);
        CustomerId retained = new CustomerId(88L);
        repository.findOrCreate(removed);
        repository.findOrCreate(retained);

        repository.deleteByCustomerId(removed);
        repository.deleteByCustomerId(removed);

        assertThat(repository.find(removed)).isEmpty();
        assertThat(repository.find(retained)).isPresent();
    }

    @Test
    void saveOverwritesTheCustomersExistingCart() {
        CustomerId customer = new CustomerId(55L);
        Cart cart = repository.findOrCreate(customer);
        cart.changeQuantity(new ProductVariantId(701L), new Quantity(1));
        repository.save(cart);

        Cart updated = repository.findOrCreate(customer);
        updated.remove(new ProductVariantId(701L));
        updated.changeQuantity(new ProductVariantId(801L), new Quantity(3));
        repository.save(updated);

        Cart restored = repository.find(customer).orElseThrow();
        assertThat(restored.id()).isEqualTo(cart.id());
        assertThat(restored.items())
                .containsExactly(new CartItem(new ProductVariantId(801L), new Quantity(3)));
    }
}
