package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedInsufficientStockException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseProductsUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.PurchasedFacts;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogPurchaseService implements PurchaseProductsUseCase {
    private final ProductRepository productRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<PurchasedProductSnapshot> purchase(List<PurchaseLine> lines) {
        List<PurchaseLine> sorted =
                lines.stream()
                        .sorted(Comparator.comparingLong(line -> line.variant().value()))
                        .toList();
        rejectDuplicates(sorted);

        List<Product> families =
                productRepository.findAllForPurchase(
                        sorted.stream().map(PurchaseLine::variant).toList());
        Map<ProductVariantId, Product> owners = new HashMap<>();
        families.forEach(
                product ->
                        product.variants()
                                .forEach(
                                        variant ->
                                                owners.put(variant.id().orElseThrow(), product)));
        List<PurchasedProductSnapshot> result = new ArrayList<>();
        try {
            for (PurchaseLine line : sorted) {
                Product owner = owners.get(line.variant());
                if (owner == null) {
                    throw new PublishedProductUnavailableException(null, null);
                }
                result.add(toSnapshot(owner.purchase(line.variant(), line.quantity())));
            }
        } catch (ProductUnavailableException exception) {
            throw new PublishedProductUnavailableException(
                    new ProductReference(exception.getProductId().value()),
                    exception.getSku() == null ? null : exception.getSku().value());
        } catch (
                com.springbootecommerce.shophappens.catalog.domain.exception
                                .InsufficientStockException
                        exception) {
            throw new PublishedInsufficientStockException(
                    new ProductReference(exception.getProductId().value()),
                    exception.getSku() == null ? null : exception.getSku().value(),
                    exception.getRequestedQuantity(),
                    exception.getAvailableQuantity());
        }
        families.forEach(productRepository::save);
        return List.copyOf(result);
    }

    private void rejectDuplicates(List<PurchaseLine> lines) {
        Set<Long> seen = new HashSet<>();
        for (PurchaseLine line : lines) {
            long value = line.variant().value();
            if (!seen.add(value)) {
                throw new IllegalArgumentException("Duplicate product reference: " + value);
            }
        }
    }

    private PurchasedProductSnapshot toSnapshot(PurchasedFacts facts) {
        return new PurchasedProductSnapshot(
                facts.variantId(),
                new ProductReference(facts.id().value()),
                facts.sku().value(),
                facts.name(),
                facts.price(),
                facts.quantity());
    }
}
