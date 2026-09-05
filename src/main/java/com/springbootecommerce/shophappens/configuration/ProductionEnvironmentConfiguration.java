package com.springbootecommerce.shophappens.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionEnvironmentConfiguration {
    public ProductionEnvironmentConfiguration(
            @Value("${SPRING_DATASOURCE_USERNAME}") String databaseUsername,
            @Value("${SPRING_DATASOURCE_PASSWORD}") String databasePassword,
            @Value("${SPRING_DATA_REDIS_PASSWORD}") String redisPassword,
            @Value("${STOREFRONT_PUBLIC_ORIGIN}") String publicOrigin,
            @Value("${APP_SIGNING_SECRET}") String signingSecret,
            @Value("${PAYMENT_PROVIDER_SECRET}") String paymentProviderSecret) {
        requireNonBlank("SPRING_DATASOURCE_USERNAME", databaseUsername);
        requireNonBlank("SPRING_DATASOURCE_PASSWORD", databasePassword);
        requireNonBlank("SPRING_DATA_REDIS_PASSWORD", redisPassword);
        requireHttpsOrigin(publicOrigin);
        requireMinimumLength("APP_SIGNING_SECRET", signingSecret, 32);
        requireNonBlank("PAYMENT_PROVIDER_SECRET", paymentProviderSecret);
    }

    private static void requireHttpsOrigin(String origin) {
        requireNonBlank("STOREFRONT_PUBLIC_ORIGIN", origin);
        if (!origin.startsWith("https://")) {
            throw new IllegalStateException("STOREFRONT_PUBLIC_ORIGIN must use HTTPS");
        }
    }

    private static void requireMinimumLength(String name, String value, int minimumLength) {
        requireNonBlank(name, value);
        if (value.length() < minimumLength) {
            throw new IllegalStateException(
                    name + " must be at least " + minimumLength + " characters");
        }
    }

    private static void requireNonBlank(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured");
        }
    }
}
