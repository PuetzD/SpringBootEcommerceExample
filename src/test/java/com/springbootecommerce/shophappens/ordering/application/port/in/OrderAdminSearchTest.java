package com.springbootecommerce.shophappens.ordering.application.port.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

public class OrderAdminSearchTest {

    @Test
    void testOrderAdminSearchWithNegativePage() {
        var exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new OrderAdminSearch(-1, 10, "query"));

        assertEquals("Page must not be negative", exception.getMessage());
    }

    @Test
    void testOrderAdminSearchWithInvalidSize() {
        var belowMinimum =
                assertThrows(
                        IllegalArgumentException.class, () -> new OrderAdminSearch(0, 0, "query"));
        var aboveMaximum =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new OrderAdminSearch(0, 101, "query"));

        assertEquals("Page size must be between 1 and 100", belowMinimum.getMessage());
        assertEquals("Page size must be between 1 and 100", aboveMaximum.getMessage());
    }

    @Test
    void testOrderAdminSearchWithBlankQuery() {
        OrderAdminSearch search = new OrderAdminSearch(0, 10, "   ");

        assertNull(search.query());
    }

    @Test
    void testOrderAdminSearchWithNullQuery() {
        OrderAdminSearch search = new OrderAdminSearch(0, 10, null);

        assertNull(search.query());
    }

    @Test
    void testOrderAdminSearchWithWhitespace() {
        OrderAdminSearch search = new OrderAdminSearch(1, 20, "valid query  ");

        assertEquals(1, search.page());
        assertEquals(20, search.size());
        assertEquals("valid query", search.query());
    }

    @Test
    void testOrderAdminSearchWithValidParameters() {
        OrderAdminSearch search = new OrderAdminSearch(1, 20, "valid query");

        assertEquals(1, search.page());
        assertEquals(20, search.size());
        assertEquals("valid query", search.query());
    }

    @Test
    void rejectsAnEmptyOrReversedPlacedRange() {
        Instant boundary = Instant.parse("2026-09-17T12:00:00Z");

        var emptyRange =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new OrderAdminSearch(0, 20, null, boundary, boundary));
        var reversedRange =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new OrderAdminSearch(
                                        0, 20, null, boundary, boundary.minusSeconds(1)));

        assertEquals("Placed range start must be before end", emptyRange.getMessage());
        assertEquals("Placed range start must be before end", reversedRange.getMessage());
    }
}
