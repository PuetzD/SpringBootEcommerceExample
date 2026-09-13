package com.springbootecommerce.shophappens.configuration;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class EmailTemplateConfiguration {
    @Bean
    @Primary
    SpringTemplateEngine emailTemplateEngine() {
        var html = resolver(".html", TemplateMode.HTML, "email/order-confirmation-html");
        var text = resolver(".txt", TemplateMode.TEXT, "email/order-confirmation-text");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolvers(Set.of(html, text));
        return engine;
    }

    private static ClassLoaderTemplateResolver resolver(
            String suffix, TemplateMode mode, String pattern) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(suffix);
        resolver.setTemplateMode(mode);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setResolvablePatterns(Set.of(pattern));
        return resolver;
    }
}
