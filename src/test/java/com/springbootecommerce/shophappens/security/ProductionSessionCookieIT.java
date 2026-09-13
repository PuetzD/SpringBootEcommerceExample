package com.springbootecommerce.shophappens.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"it", "prod"})
@Import({SecurityConfigurationIT.SecurityTestWebConfiguration.class, PasswordConfiguration.class})
@TestPropertySource(
        properties = {
            "SPRING_DATASOURCE_USERNAME=integration-test",
            "SPRING_DATASOURCE_PASSWORD=integration-test",
            "SPRING_DATA_REDIS_PASSWORD=integration-test",
            "STOREFRONT_PUBLIC_ORIGIN=https://shop.example.test",
            "APP_SIGNING_SECRET=integration-test-signing-secret-0001",
            "PAYMENT_PROVIDER_SECRET=integration-test"
        })
class ProductionSessionCookieIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private JdbcTemplate jdbc;

    @Autowired private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void disableRedisAuthentication(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.password", () -> "");
    }

    @BeforeEach
    void createCustomerAccount() {
        jdbc.update("delete from account");
        jdbc.update(
                "insert into account (email, password_hash, role, enabled) values (?,?,?,?)",
                "customer@example.com",
                passwordEncoder.encode("password"),
                "CUSTOMER",
                true);
    }

    @Test
    void keepsProductionCookiePolicyAcrossLoginAuthenticatedRequestAndLogout() throws Exception {
        var login =
                mockMvc.perform(
                                post("/login")
                                        .param("username", "customer@example.com")
                                        .param("password", "password")
                                        .header("X-Forwarded-Proto", "https")
                                        .with(csrf()))
                        .andExpect(status().isFound())
                        .andReturn();

        assertProductionSessionCookie(login.getResponse().getHeader("Set-Cookie"));
        assertThat(login.getResponse().getRedirectedUrl()).startsWith("https://");
        Cookie session = login.getResponse().getCookie("SESSION");
        assertThat(session).isNotNull();

        mockMvc.perform(
                        get("/account/security-test")
                                .header("X-Forwarded-Proto", "https")
                                .cookie(session))
                .andExpect(status().isOk());

        var logout =
                mockMvc.perform(
                                post("/logout")
                                        .header("X-Forwarded-Proto", "https")
                                        .cookie(session)
                                        .with(csrf()))
                        .andExpect(status().isFound())
                        .andReturn();

        String expiredCookie = logout.getResponse().getHeader("Set-Cookie");
        assertProductionSessionCookie(expiredCookie);
        assertThat(expiredCookie).contains("Max-Age=0");
    }

    private static void assertProductionSessionCookie(String setCookie) {
        assertThat(setCookie)
                .isNotNull()
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }
}
