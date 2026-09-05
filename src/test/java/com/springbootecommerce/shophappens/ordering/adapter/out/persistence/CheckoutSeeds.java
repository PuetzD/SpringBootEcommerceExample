package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

class CheckoutSeeds {

    record Seed(
            long customerId,
            long shippingAddressId,
            long billingAddressId,
            long productId,
            UUID cartId,
            int initialStock) {}

    record CustomerCartSeed(
            long customerId, long shippingAddressId, long billingAddressId, UUID cartId) {}

    static final BigDecimal PRODUCT_PRICE = new BigDecimal("19.99");
    static final int INITIAL_STOCK = 10;
    static final int QUANTITY = 3;

    private CheckoutSeeds() {}

    static Seed seed(JdbcTemplate jdbc) {
        long productId = seedProduct(jdbc, INITIAL_STOCK);
        CustomerCartSeed customer = seedCustomerCart(jdbc, productId, QUANTITY);
        return new Seed(
                customer.customerId(),
                customer.shippingAddressId(),
                customer.billingAddressId(),
                productId,
                customer.cartId(),
                INITIAL_STOCK);
    }

    static long seedProduct(JdbcTemplate jdbc, int initialStock) {
        UUID skuTag = UUID.randomUUID();
        return jdbc.queryForObject(
                """
                insert into product (sku, name, price, stock_quantity, active)
                values (?, ?, ?, ?, true) returning id
                """,
                Long.class,
                "ELEC-001-" + skuTag.toString().substring(0, 8),
                "Headphones",
                PRODUCT_PRICE,
                initialStock);
    }

    static CustomerCartSeed seedCustomerCart(JdbcTemplate jdbc, long productId, int quantity) {
        UUID emailTag = UUID.randomUUID();
        Long accountId =
                jdbc.queryForObject(
                        """
                        insert into account (email, password_hash, role)
                        values (?, ?, 'CUSTOMER') returning id
                        """,
                        Long.class,
                        "checkout-" + emailTag + "@example.com",
                        "encoded");
        Long customerId =
                jdbc.queryForObject(
                        "insert into customer (account_id) values (?) returning id",
                        Long.class,
                        accountId);
        Long shippingAddressId =
                jdbc.queryForObject(
                        """
                        insert into address(
                            customer_id, recipient_name, address_line_1, city,
                            postal_code, country_code, is_default_shipping, is_default_billing)
                        values (?, ?, ?, ?, ?, ?, true, false) returning id
                        """,
                        Long.class,
                        customerId,
                        "Jane Doe",
                        "123 Main St",
                        "Metropolis",
                        "10001",
                        "US");
        Long billingAddressId =
                jdbc.queryForObject(
                        """
                        insert into address(
                            customer_id, recipient_name, address_line_1, city,
                            postal_code, country_code, is_default_shipping, is_default_billing)
                        values (?, ?, ?, ?, ?, ?, false, true) returning id
                        """,
                        Long.class,
                        customerId,
                        "Jane Doe",
                        "456 Oak Ave",
                        "Metropolis",
                        "10001",
                        "US");
        UUID cartId = UUID.randomUUID();
        jdbc.update(
                "insert into customer_cart (id, customer_id) values (?, ?)", cartId, customerId);
        jdbc.update(
                "insert into customer_cart_item (cart_id, product_id, quantity) values (?, ?, ?)",
                cartId,
                productId,
                quantity);
        return new CustomerCartSeed(customerId, shippingAddressId, billingAddressId, cartId);
    }
}
