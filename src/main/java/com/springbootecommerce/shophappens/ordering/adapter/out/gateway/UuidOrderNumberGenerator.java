package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import com.springbootecommerce.shophappens.ordering.application.port.out.OrderNumberGenerator;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
class UuidOrderNumberGenerator implements OrderNumberGenerator {
    @Override
    public OrderNumber next() {
        String date =
                Instant.now()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                        .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String hex =
                java.util
                        .UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
        return new OrderNumber("ORD-" + date + "-" + hex);
    }
}
