package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JpaIntegrationEventOutboxTest {
    @Mock SpringDataOutboxRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Instant CREATED_AT = Instant.parse("2026-08-31T10:15:31Z");

    @Test
    void persistsTheVersionedOrderPlacedPayloadWithoutChangingItsWireSchema() throws Exception {
        OrderPlacedIntegrationEvent event = event();
        JpaIntegrationEventOutbox outbox =
                new JpaIntegrationEventOutbox(
                        repository, objectMapper, Clock.fixed(CREATED_AT, ZoneOffset.UTC));

        outbox.append(event);

        ArgumentCaptor<OutboxEventJpaEntity> saved =
                ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(repository).save(saved.capture());
        OutboxEventJpaEntity entity = saved.getValue();
        assertThat(entity.getEventId()).isEqualTo(event.eventId());
        assertThat(entity.getEventType()).isEqualTo("ordering.order-placed.v1");
        assertThat(entity.getAggregateKey()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);

        JsonNode payload = objectMapper.readTree(entity.getPayload());
        assertThat(payload.propertyNames())
                .containsExactlyInAnyOrder(
                        "eventId",
                        "orderId",
                        "orderNumber",
                        "customerId",
                        "occurredAt",
                        "total",
                        "items",
                        "shippingAddress",
                        "billingAddress");
        assertThat(payload.get("eventId").asString())
                .isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(payload.get("orderId").asString())
                .isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(payload.get("orderNumber").asString()).isEqualTo("ORD-20260831-ABC123DEF456");
        assertThat(payload.get("customerId").isIntegralNumber()).isTrue();
        assertThat(payload.get("customerId").longValue()).isEqualTo(42L);
        assertThat(payload.get("occurredAt").asString()).isEqualTo("2026-08-31T10:15:30Z");
        assertThat(payload.get("total").isNumber()).isTrue();
        assertThat(payload.get("total").decimalValue()).isEqualByComparingTo("39.98");
        JsonNode item = payload.get("items").get(0);
        assertThat(item.propertyNames())
                .containsExactlyInAnyOrder(
                        "productId", "sku", "productName", "unitPrice", "quantity");
        assertThat(item.get("productId").longValue()).isEqualTo(7L);
        assertThat(item.get("sku").asString()).isEqualTo("ELEC-001");
        assertThat(item.get("productName").asString()).isEqualTo("Headphones");
        assertThat(item.get("unitPrice").decimalValue()).isEqualByComparingTo("19.99");
        assertThat(item.get("quantity").intValue()).isEqualTo(2);
        assertAddress(
                payload.get("shippingAddress"),
                "123 Main St",
                "Apt 4B",
                "Metropolis",
                "NY",
                "10001");
        assertAddress(
                payload.get("billingAddress"), "456 Oak Ave", null, "Metropolis", "NY", "10001");
    }

    private static void assertAddress(
            JsonNode address,
            String addressLine1,
            String addressLine2,
            String city,
            String region,
            String postalCode) {
        assertThat(address.propertyNames())
                .containsExactlyInAnyOrder(
                        "recipientName",
                        "companyName",
                        "addressLine1",
                        "addressLine2",
                        "city",
                        "region",
                        "postalCode",
                        "countryCode",
                        "phoneNumber");
        assertThat(address.get("recipientName").asString()).isEqualTo("Jane Doe");
        assertThat(address.get("companyName").asString()).isEqualTo("Acme Inc");
        assertThat(address.get("addressLine1").asString()).isEqualTo(addressLine1);
        if (addressLine2 == null) {
            assertThat(address.get("addressLine2").isNull()).isTrue();
        } else {
            assertThat(address.get("addressLine2").asString()).isEqualTo(addressLine2);
        }
        assertThat(address.get("city").asString()).isEqualTo(city);
        assertThat(address.get("region").asString()).isEqualTo(region);
        assertThat(address.get("postalCode").asString()).isEqualTo(postalCode);
        assertThat(address.get("countryCode").asString()).isEqualTo("US");
        assertThat(address.get("phoneNumber").asString()).isEqualTo("+1-555-0100");
    }

    private static OrderPlacedIntegrationEvent event() {
        return new OrderPlacedIntegrationEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "ORD-20260831-ABC123DEF456",
                42L,
                Instant.parse("2026-08-31T10:15:30Z"),
                new BigDecimal("39.98"),
                List.of(
                        new OrderPlacedIntegrationEvent.Item(
                                7L, "ELEC-001", "Headphones", new BigDecimal("19.99"), 2)),
                new OrderPlacedIntegrationEvent.Address(
                        "Jane Doe",
                        "Acme Inc",
                        "123 Main St",
                        "Apt 4B",
                        "Metropolis",
                        "NY",
                        "10001",
                        "US",
                        "+1-555-0100"),
                new OrderPlacedIntegrationEvent.Address(
                        "Jane Doe",
                        "Acme Inc",
                        "456 Oak Ave",
                        null,
                        "Metropolis",
                        "NY",
                        "10001",
                        "US",
                        "+1-555-0100"));
    }
}
