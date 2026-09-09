package com.springbootecommerce.shophappens.ordering.adapter.out.catalog;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCatalogGateway;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CheckoutCatalogGatewayAdapter implements CheckoutCatalogGateway {
    private final BrowseCatalogUseCase catalog;

    @Override
    public Optional<CheckoutItem> find(ProductVariantId variant, int quantity) {
        return catalog.findActiveByVariantId(variant)
                .map(
                        product ->
                                new CheckoutItem(
                                        product.variant(),
                                        new ProductId(product.product().value()),
                                        product.sku(),
                                        product.name(),
                                        product.price(),
                                        quantity));
    }
}
