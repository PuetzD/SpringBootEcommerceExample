package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseProductsUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.CatalogPurchaseGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.PurchasedProduct;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class CatalogPurchaseGatewayAdapter implements CatalogPurchaseGateway {
    private final PurchaseProductsUseCase purchaseProducts;

    CatalogPurchaseGatewayAdapter(PurchaseProductsUseCase purchaseProducts) {
        this.purchaseProducts = purchaseProducts;
    }

    @Override
    public List<PurchasedProduct> purchase(List<RequestedProduct> products) {
        List<PurchaseLine> lines =
                products.stream()
                        .map(
                                p ->
                                        new PurchaseLine(
                                                new ProductReference(p.productId().value()),
                                                p.quantity()))
                        .toList();
        return purchaseProducts.purchase(lines).stream()
                .map(
                        s ->
                                new PurchasedProduct(
                                        new ProductId(s.product().value()),
                                        s.sku(),
                                        s.name(),
                                        s.unitPrice(),
                                        s.quantity()))
                .toList();
    }
}
