package com.springbootecommerce.shophappens.ordering.adapter.out.catalog;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseProductsUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.ordering.application.port.out.CatalogPurchaseGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.PurchasedProduct;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CatalogPurchaseGatewayAdapter implements CatalogPurchaseGateway {
    private final PurchaseProductsUseCase purchaseProducts;

    public CatalogPurchaseGatewayAdapter(PurchaseProductsUseCase purchaseProducts) {
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
        List<PurchasedProductSnapshot> snapshots = purchaseProducts.purchase(lines);
        validateMatchesRequested(products, snapshots);
        return snapshots.stream()
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

    private static void validateMatchesRequested(
            List<RequestedProduct> requested, List<PurchasedProductSnapshot> purchased) {
        Map<Long, Integer> requestedByProduct =
                byProduct(
                        requested.stream()
                                .map(p -> Map.entry(p.productId().value(), p.quantity()))
                                .toList());
        Map<Long, Integer> purchasedByProduct =
                byProduct(
                        purchased.stream()
                                .map(s -> Map.entry(s.product().value(), s.quantity()))
                                .toList());
        if (!requestedByProduct.equals(purchasedByProduct)) {
            throw new IllegalArgumentException("Catalog returned products that were not requested");
        }
    }

    private static Map<Long, Integer> byProduct(List<Map.Entry<Long, Integer>> lines) {
        Map<Long, Integer> quantities = new HashMap<>();
        lines.forEach(line -> quantities.merge(line.getKey(), line.getValue(), Integer::sum));
        return quantities;
    }
}
