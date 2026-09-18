package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.math.BigDecimal;

interface OrderMetricsProjection {
    BigDecimal getRevenue();
}
