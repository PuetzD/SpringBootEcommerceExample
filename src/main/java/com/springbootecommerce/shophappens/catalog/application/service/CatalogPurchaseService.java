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
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
                        .sorted(Comparator.comparingLong(line -> line.product().value()))
                        .toList();
        rejectDuplicates(sorted);

        return sorted.stream()
                .map(
                        line -> {
                            Product product =
                                    productRepository
                                            .findForPurchase(new ProductId(line.product().value()))
                                            .orElseThrow(
                                                    () ->
                                                            new PublishedProductUnavailableException(
                                                                    new ProductId(
                                                                            line.product().value()),
                                                                    null));
                            try {
                                PurchasedFacts facts = product.purchase(line.quantity());
                                productRepository.save(product);
                                return toSnapshot(facts);
                            } catch (ProductUnavailableException exception) {
                                throw new PublishedProductUnavailableException(
                                        exception.getProductId(), exception.getSku());
                            } catch (
                                    com.springbootecommerce.shophappens.catalog.domain.exception
                                                    .InsufficientStockException
                                            exception) {
                                throw new PublishedInsufficientStockException(
                                        exception.getProductId(),
                                        exception.getSku(),
                                        exception.getRequestedQuantity(),
                                        exception.getAvailableQuantity());
                            }
                        })
                .toList();
    }

    private void rejectDuplicates(List<PurchaseLine> lines) {
        Set<Long> seen = new HashSet<>();
        for (PurchaseLine line : lines) {
            long value = line.product().value();
            if (!seen.add(value)) {
                throw new IllegalArgumentException("Duplicate product reference: " + value);
            }
        }
    }

    private PurchasedProductSnapshot toSnapshot(PurchasedFacts facts) {
        return new PurchasedProductSnapshot(
                new ProductReference(facts.id().value()),
                facts.sku().value(),
                facts.name(),
                facts.price(),
                facts.quantity());
    }
}
