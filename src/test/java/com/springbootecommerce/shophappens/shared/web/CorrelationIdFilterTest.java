package com.springbootecommerce.shophappens.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void reusesValidIncomingIdAndRemovesItAfterRequest() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "4f4d8c86-9cc4-4a8d-9e35-0f5e3f4a99d7");
        FilterChain chain =
                (ignoredRequest, ignoredResponse) ->
                        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY))
                                .isEqualTo("4f4d8c86-9cc4-4a8d-9e35-0f5e3f4a99d7");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo("4f4d8c86-9cc4-4a8d-9e35-0f5e3f4a99d7");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void replacesInvalidIncomingIdWithGeneratedUuid() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "client-supplied-value");

        filter.doFilter(
                request,
                response,
                (ignoredRequest, ignoredResponse) -> {
                    var correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                    assertThat(correlationId).isNotEqualTo("client-supplied-value");
                    assertThat(correlationId).matches(CorrelationIdFilter.UUID_PATTERN);
                });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .matches(CorrelationIdFilter.UUID_PATTERN);
    }
}
