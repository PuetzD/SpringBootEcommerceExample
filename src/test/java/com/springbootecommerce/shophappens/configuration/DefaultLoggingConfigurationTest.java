package com.springbootecommerce.shophappens.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class DefaultLoggingConfigurationTest {
    private static final String SECURITY_TRACE_LEVEL = "logging.level.org.springframework.security";
    private static final String HIBERNATE_SQL_DEBUG_LEVEL = "logging.level.org.hibernate.SQL";
    private static final String SQL_BIND_TRACE_LEVEL = "logging.level.org.hibernate.orm.jdbc.bind";

    @Test
    void defaultProfileDoesNotEnableVerboseDiagnostics() throws Exception {
        PropertySource<?> source = loadDefaultDocument();

        Object showSql = source.getProperty("spring.jpa.show-sql");
        if (showSql != null) {
            assertThat(Boolean.parseBoolean(String.valueOf(showSql))).isFalse();
        }
        assertThat(source.containsProperty(SECURITY_TRACE_LEVEL)).isFalse();
        assertThat(source.containsProperty(HIBERNATE_SQL_DEBUG_LEVEL)).isFalse();
        assertThat(source.containsProperty(SQL_BIND_TRACE_LEVEL)).isFalse();
    }

    private PropertySource<?> loadDefaultDocument() throws Exception {
        List<PropertySource<?>> documents =
                new YamlPropertySourceLoader()
                        .load("application", new ClassPathResource("application.yaml"));
        return documents.get(0);
    }
}
