package com.springbootecommerce.shophappens.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class DemoDataIT extends AbstractIntegrationTest {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired BrowseCatalogUseCase catalog;

    @BeforeEach
    void seed() {
        new ResourceDatabasePopulator(new FileSystemResource("scripts/demo-data.sql"))
                .execute(dataSource);
    }

    @Test
    void everyFamilyHasOneDefaultAndPublicBrowsingWorks() {
        assertThat(jdbc.queryForObject("select count(*) from product", Long.class)).isPositive();
        assertThat(
                        jdbc.queryForObject(
                                """
                                select count(*) from product p where
                                  (select count(*) from product_variant v
                                   where v.product_id=p.id and v.is_default)=1
                                """,
                                Long.class))
                .isEqualTo(jdbc.queryForObject("select count(*) from product", Long.class));
        assertThatCode(() -> catalog.findActivePage(0, 12)).doesNotThrowAnyException();
        assertThat(
                        jdbc.queryForObject(
                                """
                                select count(*) from product_variant
                                where sku='DUCK-BRONZE' and id<>product_id and active and not is_default
                                """,
                                Long.class))
                .isEqualTo(1L);
        assertThat(
                        jdbc.queryForObject(
                                """
                                select count(*) from product_variant
                                where sku='DUCK-RETIRED' and id<>product_id and not active and not is_default
                                """,
                                Long.class))
                .isEqualTo(1L);
    }

    @Test
    void generatedAccountIdsDoNotCollideWithSeededAccounts() {
        Long id =
                jdbc.queryForObject(
                        """
                        insert into account(email,password_hash,role)
                        values ('new-demo@example.com','encoded','CUSTOMER') returning id
                        """,
                        Long.class);

        assertThat(id).isGreaterThan(2L);
    }

    @Test
    void reseedingAlsoClearsIndependentOrderAndOutboxHistory() {
        jdbc.update(
                """
                insert into customer_order(id,order_number,checkout_id,customer_id,total,placed_at)
                values (?,?,?,1,1.00,current_timestamp)
                """,
                UUID.randomUUID(),
                "DEMO-OLD",
                UUID.randomUUID());
        jdbc.update(
                """
                insert into integration_outbox
                  (event_id,event_type,aggregate_type,aggregate_key,payload,created_at)
                values (?,'ordering.order-placed.v1','Order','demo-old','{}',current_timestamp)
                """,
                UUID.randomUUID());

        seed();

        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isZero();
        assertThatCode(() -> catalog.findActivePage(0, 12)).doesNotThrowAnyException();
    }
}
