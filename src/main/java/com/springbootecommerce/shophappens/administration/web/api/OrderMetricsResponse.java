package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;

public record OrderMetricsResponse(BigDecimal revenue, String currency) {}
