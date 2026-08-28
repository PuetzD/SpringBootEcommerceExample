package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CustomerSchemaMigrationIT extends AbstractIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void customerHasIndependentIdentityAndOwnsItsAddresses() {
        Long accountId =
                jdbc.queryForObject(
                        "insert into account (email, password_hash, role) values (?, ?, ?) returning id",
                        Long.class,
                        "migration-fixture@example.com",
                        "encoded",
                        "CUSTOMER");
        Long customerId =
                jdbc.queryForObject(
                        "insert into customer (account_id) values (?) returning id",
                        Long.class,
                        accountId);
        jdbc.update(
                """
                insert into address(
                    customer_id, recipient_name, address_line_1, city,
                    postal_code, country_code, is_default_shipping, is_default_billing)
                values (?, ?, ?, ?, ?, ?, true, true)
                """,
                customerId,
                "Alex Example",
                "1 Main Street",
                "Testcity",
                "35037",
                "DE");

        Map<String, Object> customer =
                jdbc.queryForMap(
                        "select id, account_id, version from customer where account_id = ?",
                        accountId);

        assertThat(customer.get("id")).isNotNull();
        assertThat(customer.get("account_id")).isEqualTo(accountId);
        assertThat(customer.get("version")).isEqualTo(0L);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from address a join customer c on c.id = a.customer_id where c.id = ?",
                                Long.class,
                                customerId))
                .isEqualTo(1L);
    }
}
