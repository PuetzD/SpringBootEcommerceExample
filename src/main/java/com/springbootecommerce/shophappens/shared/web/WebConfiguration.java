package com.springbootecommerce.shophappens.shared.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfiguration {

    @Bean
    public UrlHandlerFilter urlHandlerFilter() {
        return UrlHandlerFilter.trailingSlashHandler("/**")
                .redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}
