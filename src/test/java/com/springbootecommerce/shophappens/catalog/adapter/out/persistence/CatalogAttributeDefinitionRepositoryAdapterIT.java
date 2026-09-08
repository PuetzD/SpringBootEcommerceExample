package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeDefinitionAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCatalogAttributeDefinitionCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCatalogAttributeException;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CatalogAttributeDefinitionRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CatalogAttributeDefinitionAdministrationUseCase definitions;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductRepository products;

    @Test
    void addingValuesRetainsTheDefinitionAndExistingAssignment() {
        definitions.create(
                new CreateCatalogAttributeDefinitionCommand(
                        "size", "Size", "SELECT", "VARIANT", true));
        definitions.addAllowedValue("size", "small", "Small");
        Long firstId =
                jdbc.queryForObject(
                        "select id from catalog_attribute_allowed_value where code='small'",
                        Long.class);
        Product product =
                products.save(
                        Product.create(
                                new Sku("ATTR-BASE"),
                                "Tee",
                                "Cotton",
                                new Money(new BigDecimal("10.00")),
                                5,
                                null,
                                Set.of()));
        jdbc.update(
                "insert into product_variant_attribute_assignment"
                        + "(variant_id,definition_id,select_value_id) "
                        + "select ?,definition_id,id from catalog_attribute_allowed_value where id=?",
                product.defaultVariant().id().orElseThrow().value(),
                firstId);

        definitions.addAllowedValue("size", "large", "Large");

        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from catalog_attribute_definition where code='size'",
                                Long.class))
                .isEqualTo(1L);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from catalog_attribute_allowed_value v"
                                        + " join catalog_attribute_definition d"
                                        + " on d.id=v.definition_id where d.code='size'",
                                Long.class))
                .isEqualTo(2L);
        assertThat(
                        jdbc.queryForObject(
                                "select select_value_id from product_variant_attribute_assignment",
                                Long.class))
                .isEqualTo(firstId);
        assertThatThrownBy(
                        () ->
                                definitions.create(
                                        new CreateCatalogAttributeDefinitionCommand(
                                                "size", "Size", "SELECT", "VARIANT", true)))
                .isInstanceOf(DuplicateCatalogAttributeException.class);
    }

    @Test
    void concurrentValueAdditionsAreBothRetained() throws Exception {
        definitions.create(
                new CreateCatalogAttributeDefinitionCommand(
                        "color", "Color", "SELECT", "VARIANT", true));
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var blue =
                    pool.submit(
                            () -> {
                                start.await();
                                return definitions.addAllowedValue("color", "blue", "Blue");
                            });
            var red =
                    pool.submit(
                            () -> {
                                start.await();
                                return definitions.addAllowedValue("color", "red", "Red");
                            });
            start.countDown();
            blue.get(10, TimeUnit.SECONDS);
            red.get(10, TimeUnit.SECONDS);

            assertThat(
                            jdbc.queryForObject(
                                    "select count(*) from catalog_attribute_allowed_value v"
                                            + " join catalog_attribute_definition d"
                                            + " on d.id=v.definition_id where d.code='color'",
                                    Long.class))
                    .isEqualTo(2L);
            assertThatThrownBy(() -> definitions.addAllowedValue("color", "blue", "Blue again"))
                    .isInstanceOf(IllegalArgumentException.class);
        } finally {
            pool.shutdownNow();
        }
    }
}
