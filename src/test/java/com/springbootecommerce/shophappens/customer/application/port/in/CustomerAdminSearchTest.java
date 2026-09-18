package com.springbootecommerce.shophappens.customer.application.port.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CustomerAdminSearchTest {

    @Test
    void rejectsNegativePage() {
        var exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CustomerAdminSearch(-1, 10, "query"));

        assertEquals("Page must not be negative", exception.getMessage());
    }

    @Test
    void rejectsInvalidSize() {
        var belowMinimum =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CustomerAdminSearch(0, 0, "query"));
        var aboveMaximum =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CustomerAdminSearch(0, 101, "query"));

        assertEquals("Page size must be between 1 and 100", belowMinimum.getMessage());
        assertEquals("Page size must be between 1 and 100", aboveMaximum.getMessage());
    }

    @Test
    void normalizesBlankQueryToNull() {
        assertNull(new CustomerAdminSearch(0, 10, "   ").query());
    }

    @Test
    void stripsQueryWhitespace() {
        assertEquals("Ada", new CustomerAdminSearch(0, 10, "  Ada  ").query());
    }

    @Test
    void rejectsAnEmptyOrReversedCreationRange() {
        Instant boundary = Instant.parse("2026-09-17T12:00:00Z");

        var emptyRange =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CustomerAdminSearch(0, 20, null, boundary, boundary));
        var reversedRange =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new CustomerAdminSearch(
                                        0, 20, null, boundary, boundary.minusSeconds(1)));

        assertEquals("Creation range start must be before end", emptyRange.getMessage());
        assertEquals("Creation range start must be before end", reversedRange.getMessage());
    }
}
