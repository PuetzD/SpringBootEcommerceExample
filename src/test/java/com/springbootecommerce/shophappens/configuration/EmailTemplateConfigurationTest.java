package com.springbootecommerce.shophappens.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.thymeleaf.spring6.SpringTemplateEngine;

class EmailTemplateConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(ThymeleafAutoConfiguration.class))
                    .withUserConfiguration(EmailTemplateConfiguration.class);

    @Test
    void leavesTheWebTemplateEngineToSpringBoot() {
        contextRunner.run(
                context ->
                        assertThat(context)
                                .getBeans(SpringTemplateEngine.class)
                                .containsOnlyKeys("templateEngine"));
    }
}
