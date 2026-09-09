package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReviewChangedException;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.ordering.application.port.out.CatalogPurchaseGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutLock;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerAddressGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderNumberGenerator;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.ordering.application.port.out.PurchasedProduct;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService implements PlaceOrderUseCase {
    private final OrderRepository orders;
    private final CustomerAddressGateway addresses;
    private final CustomerCartGateway carts;
    private final CatalogPurchaseGateway catalog;
    private final OrderNumberGenerator numbers;
    private final CheckoutLock checkoutLock;
    private final IntegrationEventOutbox outbox;
    private final Clock clock;

    @Override
    @Transactional
    public PlacedOrder place(PlaceOrderCommand command) {
        CustomerId cid = command.customer();
        CheckoutId ckid = new CheckoutId(command.checkout().value());

        checkoutLock.acquire(cid, ckid);

        Optional<Order> existing = orders.findByCheckout(cid, ckid);
        if (existing.isPresent()) {
            return toPlacedOrder(existing.get());
        }

        CheckoutReview review = command.review();
        if (review == null
                || !review.customer().equals(cid)
                || !clock.instant().isBefore(review.expiresAt())) {
            throw new CheckoutReviewChangedException();
        }

        CheckoutCart cart = carts.load(cid);
        if (cart.empty()) {
            throw new EmptyCheckoutException();
        }

        OrderAddress shipping = addresses.shipping(cid, command.shippingAddress());
        OrderAddress billing = addresses.billing(cid, command.billingAddress());

        List<RequestedProduct> requested =
                cart.products().stream()
                        .map(p -> new RequestedProduct(p.variantId(), p.quantity()))
                        .toList();
        List<PurchasedProduct> purchased = catalog.purchase(requested);
        List<CheckoutItem> purchasedFacts =
                purchased.stream()
                        .map(
                                product ->
                                        new CheckoutItem(
                                                product.variantId(),
                                                product.productId(),
                                                product.sku(),
                                                product.name(),
                                                product.unitPrice(),
                                                product.quantity()))
                        .sorted(Comparator.comparingLong(item -> item.variant().value()))
                        .toList();
        List<CheckoutItem> reviewedFacts =
                review.items().stream()
                        .sorted(Comparator.comparingLong(item -> item.variant().value()))
                        .toList();
        if (!reviewedFacts.equals(purchasedFacts)) {
            throw new CheckoutReviewChangedException();
        }

        List<OrderItem> items =
                purchased.stream()
                        .map(
                                p ->
                                        new OrderItem(
                                                p.variantId(),
                                                p.productId(),
                                                p.sku(),
                                                p.name(),
                                                p.unitPrice(),
                                                p.quantity()))
                        .toList();

        OrderNumber number = numbers.next();
        Instant placedAt = clock.instant();

        Order order =
                Order.place(
                        OrderId.random(), number, ckid, cid, items, shipping, billing, placedAt);
        Order saved = orders.save(order);
        outbox.append(OrderPlacedIntegrationEvent.from(saved));
        carts.clear(cid);

        return toPlacedOrder(saved);
    }

    private PlacedOrder toPlacedOrder(Order o) {
        return new PlacedOrder(
                new OrderReference(o.orderId().value()),
                o.orderNumber().value(),
                o.total(),
                o.placedAt());
    }
}
