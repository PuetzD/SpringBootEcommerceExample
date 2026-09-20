package com.springbootecommerce.shophappens.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class SchemaOwnershipIT extends AbstractIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void customerProfileDoesNotHaveAnAccountForeignKey() {
        Integer count =
                jdbc.queryForObject(
                        """
                        select count(*)
                        from information_schema.table_constraints tc
                        join information_schema.key_column_usage kcu
                          on tc.constraint_schema = kcu.constraint_schema
                         and tc.constraint_name = kcu.constraint_name
                         and tc.table_name = kcu.table_name
                        join information_schema.constraint_column_usage ccu
                          on tc.constraint_schema = ccu.constraint_schema
                         and tc.constraint_name = ccu.constraint_name
                        where tc.constraint_schema = current_schema()
                          and tc.table_name = 'customer'
                          and tc.constraint_type = 'FOREIGN KEY'
                          and kcu.column_name = 'account_id'
                          and ccu.table_name = 'account'
                          and ccu.column_name = 'id'
                        """,
                        Integer.class);

        assertThat(count).isZero();
    }

    @Test
    void freshSchemaContainsAllContextTables() {
        Set<String> expected =
                Set.of(
                        "account",
                        "customer",
                        "address",
                        "category",
                        "product",
                        "product_category",
                        "product_variant",
                        "customer_cart",
                        "customer_cart_item",
                        "consumed_guest_cart",
                        "customer_order",
                        "order_item",
                        "order_address",
                        "integration_outbox",
                        "catalog_attribute_definition",
                        "catalog_attribute_allowed_value",
                        "product_attribute_assignment",
                        "product_variant_attribute_assignment",
                        "order_confirmation_delivery");

        Set<String> actual =
                Set.copyOf(
                        jdbc.queryForList(
                                """
                                select table_name
                                from information_schema.tables
                                where table_schema = current_schema()
                                  and table_type = 'BASE TABLE'
                                """,
                                String.class));

        assertThat(actual).containsAll(expected);
    }
}
