package com.springbootecommerce.shophappens.cart.application;

import com.springbootecommerce.shophappens.cart.domain.Cart;
import com.springbootecommerce.shophappens.cart.domain.Quantity;
import com.springbootecommerce.shophappens.cart.persistence.CartRepository;
import com.springbootecommerce.shophappens.catalog.application.CatalogQueryService;
import com.springbootecommerce.shophappens.catalog.domain.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository repository;
    private final CatalogQueryService catalog;

    public CartService(CartRepository repository, CatalogQueryService catalog) {
        this.repository = repository;
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public CartDetails getDetails(Long customerId) {
        var cart = repository.findByCustomerId(customerId).orElse(null);
        if (cart == null || cart.isEmpty()) {
            return new CartDetails(List.of(), Money.zero());
        }

        var lines = new ArrayList<CartLine>();
        var total = Money.zero();
        for (var item : cart.items()) {
            var product = catalog.findActiveProductById(item.productId());
            if (product.isEmpty()) {
                continue;
            }
            var summary = product.get();
            var lineTotal = summary.price().multiply(item.quantity().value());
            lines.add(
                    new CartLine(
                            summary.id(),
                            summary.sku(),
                            summary.name(),
                            summary.price(),
                            item.quantity().value(),
                            lineTotal));
            total = total.add(lineTotal);
        }
        return new CartDetails(lines, total);
    }

    @Transactional
    public void addProduct(Long customerId, Long productId, int quantity) {
        if (catalog.findActiveProductById(productId).isEmpty()) {
            throw new ProductUnavailableException(productId, null);
        }
        repository.ensureExistsForCustomer(customerId);
        var cart =
                repository
                        .findByCustomerId(customerId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Expected a cart for customer " + customerId));
        cart.addProduct(productId, new Quantity(quantity));
    }

    @Transactional
    public void changeQuantity(Long customerId, Long productId, int quantity) {
        repository
                .findByCustomerId(customerId)
                .ifPresent(cart -> cart.changeQuantity(productId, quantity));
    }

    @Transactional(readOnly = true)
    public CartSelection getSelection(Long customerId) {
        var cart = repository.findByCustomerId(customerId).orElse(null);
        if (cart == null || cart.isEmpty()) {
            return new CartSelection(customerId, List.of());
        }
        var items =
                cart.items().stream()
                        .map(
                                item ->
                                        new CartSelectionItem(
                                                item.productId(), item.quantity().value()))
                        .toList();
        return new CartSelection(customerId, items);
    }

    @Transactional
    public void clear(Long customerId) {
        repository.findByCustomerId(customerId).ifPresent(Cart::clear);
    }
}
