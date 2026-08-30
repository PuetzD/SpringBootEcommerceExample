package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.adapter.out.cart.CustomerCartGatewayAdapter;
import com.springbootecommerce.shophappens.ordering.adapter.out.persistence.CheckoutSeeds.Seed;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CheckoutRollbackIT extends AbstractIntegrationTest {
    @Autowired PlaceOrderUseCase checkout;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CustomerCartGatewayAdapter cartGateway;

    @Test
    void catalogSucceedsButCartClearThrowsAndRollsBack() {
        Seed seed = CheckoutSeeds.seed(jdbc);
        CheckoutReference checkoutId = new CheckoutReference(UUID.randomUUID());
        PlaceOrderCommand command =
                new PlaceOrderCommand(
                        new CustomerReference(seed.customerId()),
                        checkoutId,
                        new AddressReference(seed.shippingAddressId()),
                        new AddressReference(seed.billingAddressId()));

        List<RequestedProduct> cartItems =
                jdbc.query(
                        "select product_id, quantity from customer_cart_item where cart_id = ?",
                        cartRowMapper(),
                        seed.cartId());
        when(cartGateway.load(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new CheckoutCart(cartItems));
        doThrow(new RuntimeException("cart clear failure"))
                .when(cartGateway)
                .clear(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> checkout.place(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("cart clear failure");

        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from customer_order where customer_id = ? and checkout_id = ?",
                                Long.class,
                                seed.customerId(),
                                checkoutId.value()))
                .isZero();
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product where id = ?",
                                Integer.class,
                                seed.productId()))
                .isEqualTo(seed.initialStock());
    }

    private static RowMapper<RequestedProduct> cartRowMapper() {
        return (rs, rowNum) ->
                new RequestedProduct(
                        new ProductId(rs.getLong("product_id")), rs.getInt("quantity"));
    }
}
