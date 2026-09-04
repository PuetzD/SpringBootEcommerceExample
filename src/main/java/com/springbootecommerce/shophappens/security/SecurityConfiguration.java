package com.springbootecommerce.shophappens.security;

import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    @Order(1)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/api/admin/**")
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/admin/login")
                                        .permitAll()
                                        .anyRequest()
                                        .hasRole("ADMIN"))
                .formLogin(
                        form ->
                                form.loginPage("/admin/login")
                                        .loginProcessingUrl("/admin/login")
                                        .defaultSuccessUrl("/admin/products", false)
                                        .permitAll())
                .logout(
                        logout ->
                                logout.logoutUrl("/admin/logout")
                                        .logoutSuccessUrl("/admin/login?logout")
                                        .permitAll())
                .exceptionHandling(
                        exception ->
                                exception
                                        .defaultAuthenticationEntryPointFor(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                request ->
                                                        request.getServletPath()
                                                                .startsWith("/api/admin/"))
                                        .defaultAccessDeniedHandlerFor(
                                                new HttpStatusAccessDeniedHandler(
                                                        HttpStatus.FORBIDDEN),
                                                request ->
                                                        request.getServletPath()
                                                                .startsWith("/api/admin/"))
                                        .accessDeniedPage("/403"));
        applySecurityHeaders(http);

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain storefrontSecurityFilterChain(
            HttpSecurity http, CartMergingAuthenticationSuccessHandler successHandler)
            throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/catalog/**",
                                                "/cart/**",
                                                "/login",
                                                "/register/**",
                                                "/error",
                                                "/403",
                                                "/actuator/health/**",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/favicon.ico")
                                        .permitAll()
                                        .requestMatchers(
                                                "/account/**",
                                                "/profile/**",
                                                "/checkout/**",
                                                "/orders/**")
                                        .hasRole("CUSTOMER")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        form -> form.loginPage("/login").successHandler(successHandler).permitAll())
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .logoutSuccessUrl("/login?logout")
                                        .permitAll())
                .exceptionHandling(exception -> exception.accessDeniedPage("/403"));
        applySecurityHeaders(http);

        return http.build();
    }

    private static void applySecurityHeaders(HttpSecurity http) throws Exception {
        http.headers(
                headers -> {
                    headers.contentSecurityPolicy(
                            csp ->
                                    csp.policyDirectives(
                                            "default-src 'self'; "
                                                    + "script-src 'self'; "
                                                    + "style-src 'self' 'unsafe-inline'; "
                                                    + "img-src 'self' data:; "
                                                    + "font-src 'self'; "
                                                    + "object-src 'none'; "
                                                    + "base-uri 'self'; "
                                                    + "frame-ancestors 'none'"));
                    headers.frameOptions(frame -> frame.deny());
                    headers.referrerPolicy(
                            referrer ->
                                    referrer.policy(
                                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                    headers.permissionsPolicyHeader(
                            permissions ->
                                    permissions.policy("geolocation=(), microphone=(), camera=()"));
                    headers.httpStrictTransportSecurity(
                            hsts ->
                                    hsts.includeSubDomains(true)
                                            .preload(true)
                                            .maxAgeInSeconds(31536000));
                });
    }
}
