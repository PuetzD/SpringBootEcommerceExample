package com.springbootecommerce.shophappens.security;

import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    @Order(1)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**")
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
                                        .defaultSuccessUrl("/admin/index", false)
                                        .permitAll())
                .logout(
                        logout ->
                                logout.logoutUrl("/admin/logout")
                                        .logoutSuccessUrl("/admin/login?logout")
                                        .permitAll())
                .exceptionHandling(exception -> exception.accessDeniedPage("/403"));

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

        return http.build();
    }
}
