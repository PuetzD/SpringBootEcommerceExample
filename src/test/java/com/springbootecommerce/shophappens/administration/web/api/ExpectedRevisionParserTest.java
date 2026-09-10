package com.springbootecommerce.shophappens.administration.web.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ExpectedRevisionParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"\"0\"", "\"42\""})
    void parsesExactlyOneQuotedNonNegativeDecimalRevision(String header) {
        assertThat(ExpectedRevisionParser.parse(header))
                .isEqualTo(Long.parseLong(header.substring(1, header.length() - 1)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(
            strings = {"0", "-1", "\"-1\"", "\"four\"", "\"4\", \"5\"", "\"9223372036854775808\""})
    void rejectsMissingMalformedAndOutOfRangeRevisionHeaders(String header) {
        assertThatThrownBy(() -> ExpectedRevisionParser.parse(header))
                .isInstanceOf(InvalidCatalogOperationException.class)
                .hasMessage("If-Match must be a quoted revision");
    }
}
