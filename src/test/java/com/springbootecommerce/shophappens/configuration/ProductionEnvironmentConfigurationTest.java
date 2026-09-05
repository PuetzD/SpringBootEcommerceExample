package com.springbootecommerce.shophappens.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

class ProductionEnvironmentConfigurationTest {
    @Test
    void rejectsMissingProductionProperties() {
        try (var context = productionContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "test-production-properties",
                                    java.util.Map.of(
                                            "SPRING_DATASOURCE_USERNAME", "",
                                            "SPRING_DATASOURCE_PASSWORD", "db-password",
                                            "SPRING_DATA_REDIS_PASSWORD", "redis-password",
                                            "STOREFRONT_PUBLIC_ORIGIN", "https://shop.example",
                                            "APP_SIGNING_SECRET",
                                                    "signing-secret-32-characters-long",
                                            "PAYMENT_PROVIDER_SECRET", "provider-secret")));
            assertThatThrownBy(context::refresh)
                    .isInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("SPRING_DATASOURCE_USERNAME must be configured");
        }
    }

    @Test
    void loadsWhenAllProductionPropertiesArePresent() {
        try (var context = productionContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "test-production-properties",
                                    java.util.Map.of(
                                            "SPRING_DATASOURCE_USERNAME", "db-user",
                                            "SPRING_DATASOURCE_PASSWORD", "db-password",
                                            "SPRING_DATA_REDIS_PASSWORD", "redis-password",
                                            "STOREFRONT_PUBLIC_ORIGIN", "https://shop.example",
                                            "APP_SIGNING_SECRET",
                                                    "signing-secret-32-characters-long",
                                            "PAYMENT_PROVIDER_SECRET", "provider-secret")));
            context.refresh();

            assertThat(context.getBean(ProductionEnvironmentConfiguration.class)).isNotNull();
        }
    }

    private AnnotationConfigApplicationContext productionContext() {
        var context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("prod");
        context.register(ProductionEnvironmentConfiguration.class);
        return context;
    }
}
