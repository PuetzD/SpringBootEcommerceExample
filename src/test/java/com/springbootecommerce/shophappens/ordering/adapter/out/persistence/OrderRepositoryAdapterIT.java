package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class OrderRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired OrderRepository repository;
    @Autowired JdbcTemplate jdbc;

    private static final CustomerId CUSTOMER = new CustomerId(42L);

    @Test
    void savesAnOrderAndRestoresEveryField() {
        CheckoutId checkout = new CheckoutId(UUID.randomUUID());
        Order original = sampleOrder("ORD-20260828-ABC111DEF111", CUSTOMER, checkout);

        Order saved = repository.save(original);

        Order restored = repository.findByCheckout(CUSTOMER, checkout).orElseThrow();

        assertThat(saved.orderId()).isEqualTo(original.orderId());
        assertThat(restored).isEqualTo(saved);
        assertThat(restored.orderId()).isEqualTo(original.orderId());
        assertThat(restored.orderNumber()).isEqualTo(original.orderNumber());
        assertThat(restored.checkoutId()).isEqualTo(original.checkoutId());
        assertThat(restored.customerId()).isEqualTo(original.customerId());
        assertThat(restored.placedAt()).isEqualTo(original.placedAt());
        assertThat(restored.total()).isEqualTo(original.total());
        assertThat(restored.total()).isEqualTo(new Money(new BigDecimal("68.48")));
        assertThat(restored.items()).containsExactlyElementsOf(original.items());
        assertThat(restored.items())
                .extracting(OrderItem::lineTotal)
                .containsExactly(
                        new Money(new BigDecimal("39.98")), new Money(new BigDecimal("28.50")));
        assertThat(restored.shippingAddress()).isEqualTo(original.shippingAddress());
        assertThat(restored.billingAddress()).isEqualTo(original.billingAddress());
    }

    @Test
    void findAllByCustomerRestoresEveryStoredOrder() {
        CustomerId customer = new CustomerId(4242L);
        Order original =
                sampleOrder(
                        "ORD-20260828-ABC222DEF222", customer, new CheckoutId(UUID.randomUUID()));
        repository.save(original);

        List<Order> restored = repository.findAllByCustomer(customer);

        assertThat(restored).containsExactly(original);
        assertThat(repository.findAllByCustomer(new CustomerId(123L))).isEmpty();
    }

    @Test
    void storedRowsCarryTheComputedLineTotalsAndTotal() {
        Order original =
                sampleOrder(
                        "ORD-20260828-ABC333DEF333", CUSTOMER, new CheckoutId(UUID.randomUUID()));
        repository.save(original);
        UUID orderId = original.orderId().value();

        List<BigDecimal> lineTotals =
                jdbc.queryForList(
                        """
                        select line_total from order_item
                        where order_id = ? order by line_number
                        """,
                        BigDecimal.class,
                        orderId);
        assertThat(lineTotals).containsExactly(new BigDecimal("39.98"), new BigDecimal("28.50"));
        assertThat(
                        jdbc.queryForList(
                                "select line_number from order_item where order_id = ? order by line_number",
                                Integer.class,
                                orderId))
                .containsExactly(0, 1);
        assertThat(
                        jdbc.queryForObject(
                                "select total from customer_order where id = ?",
                                BigDecimal.class,
                                orderId))
                .isEqualByComparingTo(new BigDecimal("68.48"));
        assertThat(
                        jdbc.queryForList(
                                "select address_role from order_address where order_id = ?",
                                String.class,
                                orderId))
                .containsExactlyInAnyOrder("SHIPPING", "BILLING");
    }

    private static Order sampleOrder(String orderNumber, CustomerId customer, CheckoutId checkout) {
        return Order.place(
                OrderId.random(),
                new OrderNumber(orderNumber),
                checkout,
                customer,
                List.of(
                        new OrderItem(
                                new ProductId(7L),
                                "ELEC-001",
                                "Headphones",
                                new Money(new BigDecimal("19.99")),
                                2),
                        new OrderItem(
                                new ProductId(9L),
                                "TOY-003",
                                "Building Blocks",
                                new Money(new BigDecimal("9.50")),
                                3)),
                shippingAddress(),
                billingAddress(),
                Instant.parse("2026-08-28T08:00:00Z"));
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
}
