package com.springbootecommerce.shophappens.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@AutoConfigureMockMvc
@Import({SecurityConfigurationIT.SecurityTestWebConfiguration.class, PasswordConfiguration.class})
class SecurityConfigurationIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private JdbcTemplate jdbc;

    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearAccounts() {
        jdbc.update("delete from account");
    }

    @Test
    void permitsAnonymousPublicRoutes() throws Exception {
        mockMvc.perform(get("/catalog/security-test")).andExpect(status().isOk());
        mockMvc.perform(get("/cart/security-test")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/login")).andExpect(status().isOk());
    }

    @Test
    void sendsSecureBrowserHeadersOnPublicAndAdminResponses() throws Exception {
        mockMvc.perform(get("/catalog/security-test").secure(true))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        containsString("default-src 'self'")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(
                        header().string(
                                        "Permissions-Policy",
                                        "geolocation=(), microphone=(), camera=()"))
                .andExpect(
                        header().string("Strict-Transport-Security", containsString("max-age=")));

        mockMvc.perform(get("/admin/login").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void rendersHomepageInThePublicIndexWithSeoMetadata() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/index"));
    }

    @Test
    void rendersLoginAndAccessDeniedInsideThePublicShell() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/login"));

        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/403"));
    }

    @Test
    void keepsAdminLoginOutsideThePublicShell() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void redirectsAnonymousCustomerAndAdminRequestsToTheirLoginPages() throws Exception {
        mockMvc.perform(get("/account/security-test"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/login")));
        mockMvc.perform(get("/admin/security-test"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/admin/login")));
    }

    @Test
    void grantsCustomerRoutesOnlyToCustomers() throws Exception {
        mockMvc.perform(get("/account/security-test").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/account/security-test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void grantsAdminRoutesOnlyToAdmins() throws Exception {
        mockMvc.perform(get("/admin/security-test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/security-test").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void requiresCsrfForCustomerAndAdminStateChanges() throws Exception {
        mockMvc.perform(post("/order/security-test").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        post("/order/security-test")
                                .with(user("customer").roles("CUSTOMER"))
                                .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/security-test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        post("/admin/security-test")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatesKnownCustomerAndRejectsUnknownAndDisabledAccounts() throws Exception {
        createAccount("customer@example.com", "CUSTOMER", true);
        createAccount("disabled@example.com", "CUSTOMER", false);

        mockMvc.perform(formLogin("/login").user("customer@example.com").password("password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));
        mockMvc.perform(formLogin("/login").user("missing@example.com").password("password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/login?error")));
        mockMvc.perform(formLogin("/login").user("disabled@example.com").password("password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/login?error")));
    }

    @Test
    void authenticatesKnownAdministrator() throws Exception {
        createAccount("admin@example.com", "ADMIN", true);

        mockMvc.perform(formLogin("/admin/login").user("admin@example.com").password("password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/admin/products"));
    }

    @Test
    void redirectsCustomersToTheirOriginalRequestAfterLogin() throws Exception {
        createAccount("customer@example.com", "CUSTOMER", true);
        var protectedRequest =
                mockMvc.perform(get("/account/security-test"))
                        .andExpect(status().isFound())
                        .andReturn();

        mockMvc.perform(
                        post("/login")
                                .param("username", "customer@example.com")
                                .param("password", "password")
                                .cookie(protectedRequest.getResponse().getCookie("SESSION"))
                                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/account/security-test")));
    }

    @Test
    void deniesCustomerAccessAfterAdminLogin() throws Exception {
        createAccount("customer@example.com", "CUSTOMER", true);

        var loginResult =
                mockMvc.perform(
                                formLogin("/admin/login")
                                        .user("customer@example.com")
                                        .password("password"))
                        .andExpect(status().isFound())
                        .andReturn();

        mockMvc.perform(
                        get("/admin/products")
                                .cookie(loginResult.getResponse().getCookie("SESSION")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void logsOutAuthenticatedCustomers() throws Exception {
        createAccount("customer@example.com", "CUSTOMER", true);
        var loginResult =
                mockMvc.perform(
                                formLogin("/login")
                                        .user("customer@example.com")
                                        .password("password"))
                        .andExpect(status().isFound())
                        .andReturn();

        mockMvc.perform(
                        post("/logout")
                                .cookie(loginResult.getResponse().getCookie("SESSION"))
                                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/login?logout"));

        mockMvc.perform(
                        get("/account/security-test")
                                .cookie(loginResult.getResponse().getCookie("SESSION")))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/login")));
    }

    private void createAccount(String email, String role, boolean enabled) {
        jdbc.update(
                "insert into account (email, password_hash, role, enabled) values (?,?,?,?)",
                email,
                passwordEncoder.encode("password"),
                role,
                enabled);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityTestWebConfiguration {
        @Bean
        SecurityTestController securityTestController() {
            return new SecurityTestController();
        }
    }

    @RestController
    static class SecurityTestController {
        @GetMapping({"/catalog/security-test", "/cart/security-test"})
        String publicRoute() {
            return "public";
        }

        @GetMapping("/account/security-test")
        String customerRoute() {
            return "customer";
        }

        @PostMapping("/order/security-test")
        String customerStateChange() {
            return "customer-updated";
        }

        @GetMapping({"/admin/index", "/admin/security-test"})
        String adminRoute() {
            return "admin";
        }

        @PostMapping("/admin/security-test")
        String adminStateChange() {
            return "admin-updated";
        }
    }
}
