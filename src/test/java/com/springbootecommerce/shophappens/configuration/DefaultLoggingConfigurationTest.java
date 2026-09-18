package com.springbootecommerce.shophappens.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class DefaultLoggingConfigurationTest {
    private static final String SECURITY_TRACE_LEVEL = "logging.level.org.springframework.security";
    private static final String HIBERNATE_SQL_DEBUG_LEVEL = "logging.level.org.hibernate.SQL";
    private static final String SQL_BIND_TRACE_LEVEL = "logging.level.org.hibernate.orm.jdbc.bind";
    private static final String LOG_FILE_NAME = "logging.file.name";
    private static final String MAX_FILE_SIZE = "logging.logback.rollingpolicy.max-file-size";
    private static final String MAX_HISTORY = "logging.logback.rollingpolicy.max-history";
    private static final String TOTAL_SIZE_CAP = "logging.logback.rollingpolicy.total-size-cap";

    @Test
    void defaultProfileDoesNotEnableVerboseDiagnostics() throws Exception {
        PropertySource<?> source = load("application.yaml");

        Object showSql = source.getProperty("spring.jpa.show-sql");
        if (showSql != null) {
            assertThat(Boolean.parseBoolean(String.valueOf(showSql))).isFalse();
        }
        assertThat(source.containsProperty(SECURITY_TRACE_LEVEL)).isFalse();
        assertThat(source.containsProperty(HIBERNATE_SQL_DEBUG_LEVEL)).isFalse();
        assertThat(source.containsProperty(SQL_BIND_TRACE_LEVEL)).isFalse();
    }

    @Test
    void defaultProfileEnablesBoundedRollingFileLogging() throws Exception {
        PropertySource<?> source = load("application.yaml");

        assertThat(source.getProperty(LOG_FILE_NAME))
                .isEqualTo("${LOGGING_FILE_NAME:logs/ecommerce.log}");
        assertThat(source.getProperty(MAX_FILE_SIZE)).isEqualTo("10MB");
        assertThat(source.getProperty(MAX_HISTORY)).isEqualTo(30);
        assertThat(source.getProperty(TOTAL_SIZE_CAP)).isEqualTo("1GB");
    }

    @Test
    void testRuntimeDisablesFileLogging() throws Exception {
        List<PropertySource<?>> documents =
                new PropertiesPropertySourceLoader()
                        .load("application-test", new ClassPathResource("application.properties"));

        assertThat(documents.getFirst().getProperty(LOG_FILE_NAME)).isEqualTo("");
    }

    private PropertySource<?> load(String resource) throws Exception {
        List<PropertySource<?>> documents =
                new YamlPropertySourceLoader().load(resource, new ClassPathResource(resource));
        return documents.getFirst();
    }
}
