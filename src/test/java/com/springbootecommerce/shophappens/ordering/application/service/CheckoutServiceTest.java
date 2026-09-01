package com.springbootecommerce.shophappens.ordering.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
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
import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
    @Mock OrderRepository orders;
    @Mock CustomerAddressGateway addresses;
    @Mock CustomerCartGateway carts;
    @Mock CatalogPurchaseGateway catalog;
    @Mock OrderNumberGenerator numbers;
    @Mock CheckoutLock checkoutLock;
    @Mock IntegrationEventOutbox outbox;
    CheckoutService service;

    private static final UUID CHECKOUT_ID = UUID.randomUUID();
    private static final Instant PLACED_AT = Instant.parse("2026-08-31T10:15:30Z");

    @BeforeEach
    void setUp() {
        service =
                new CheckoutService(
                        orders,
                        addresses,
                        carts,
                        catalog,
                        numbers,
                        checkoutLock,
                        outbox,
                        Clock.fixed(PLACED_AT, ZoneOffset.UTC));
    }

    @Test
    void placesInRequiredOrderAndReturnsPersistedResult() {
        var command = command(42L, CHECKOUT_ID, 11L, 12L);
        when(orders.findByCheckout(new CustomerId(42L), new CheckoutId(CHECKOUT_ID)))
                .thenReturn(Optional.empty());
        when(carts.load(new CustomerId(42L))).thenReturn(cartWith(7L, 2));
        when(addresses.shipping(new CustomerId(42L), 11L)).thenReturn(shippingAddress());
        when(addresses.billing(new CustomerId(42L), 12L)).thenReturn(billingAddress());
        when(catalog.purchase(List.of(new RequestedProduct(new ProductId(7L), 2))))
                .thenReturn(List.of(purchasedProduct(7L, 2, "19.99")));
        when(numbers.next()).thenReturn(new OrderNumber("ORD-20260828-ABC123DEF456"));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlacedOrder result = service.place(command);

        assertThat(result.total()).isEqualTo(new Money(new BigDecimal("39.98")));
        assertThat(result.placedAt()).isEqualTo(PLACED_AT);
        ArgumentCaptor<OrderPlacedIntegrationEvent> event =
                ArgumentCaptor.forClass(OrderPlacedIntegrationEvent.class);
        org.mockito.Mockito.verify(outbox).append(event.capture());
        assertThat(OrderPlacedIntegrationEvent.EVENT_TYPE).isEqualTo("ordering.order-placed.v1");
        assertThat(event.getValue().orderId()).isEqualTo(result.order().value());
    }

    @Test
    void returnsExistingOrderWithoutTouchingCartOrCatalog() {
        when(orders.findByCheckout(new CustomerId(42L), new CheckoutId(CHECKOUT_ID)))
                .thenReturn(Optional.of(existingOrder()));

        PlacedOrder result = service.place(command(42L, CHECKOUT_ID, 11L, 12L));

        assertThat(result.orderNumber()).isEqualTo("ORD-20260828-EXISTING0101");
        verifyNoInteractions(carts, catalog, addresses, numbers);
    }

    private static PlaceOrderCommand command(
            long customer, UUID checkout, long shipping, long billing) {
        return new PlaceOrderCommand(
                new CustomerReference(customer),
                new CheckoutReference(checkout),
                new AddressReference(shipping),
                new AddressReference(billing));
    }

    private static CheckoutCart cartWith(long productId, int quantity) {
        return new CheckoutCart(List.of(new RequestedProduct(new ProductId(productId), quantity)));
    }

    private static OrderAddress shippingAddress() {
        return new OrderAddress(
                AddressRole.SHIPPING,
                "Jane Doe",
                "Acme Inc",
                "123 Main St",
                "Apt 4B",
                "Metropolis",
                "NY",
                "10001",
                "US",
                "+1-555-0100");
    }

    private static OrderAddress billingAddress() {
        return new OrderAddress(
                AddressRole.BILLING,
                "Jane Doe",
                "Acme Inc",
                "456 Oak Ave",
                null,
                "Metropolis",
                "NY",
                "10001",
                "US",
                "+1-555-0100");
    }

    private static PurchasedProduct purchasedProduct(long productId, int quantity, String price) {
        return new PurchasedProduct(
                new ProductId(productId),
                "ELEC-001",
                "Headphones",
                new Money(new BigDecimal(price)),
                quantity);
    }

    private static Order existingOrder() {
        return Order.restore(
                OrderId.random(),
                new OrderNumber("ORD-20260828-EXISTING0101"),
                new CheckoutId(CHECKOUT_ID),
                new CustomerId(42L),
                List.of(
                        new OrderItem(
                                new ProductId(7L),
                                "ELEC-001",
                                "Headphones",
                                new Money(new BigDecimal("19.99")),
                                1)),
                shippingAddress(),
                billingAddress(),
                Instant.parse("2026-08-28T08:00:00Z"),
                new Money(new BigDecimal("19.99")));
    }
}
