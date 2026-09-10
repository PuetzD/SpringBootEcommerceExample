package com.springbootecommerce.shophappens.administration.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.administration.web.api.ApiErrorResponse;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCategoryException;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleCategoryRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class AdminApiExceptionHandlerTest {
    private final AdminApiExceptionHandler handler = new AdminApiExceptionHandler();

    @Test
    void validationReturnsFirstFieldMessageInThePublishedEnvelope() throws Exception {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "request");
        errors.addError(new FieldError("request", "name", "First name message"));
        errors.addError(new FieldError("request", "name", "Second name message"));
        Method method =
                AdminApiExceptionHandlerTest.class.getDeclaredMethod("request", Object.class);

        ApiErrorResponse body =
                handler.handleValidation(
                                new MethodArgumentNotValidException(
                                        new MethodParameter(method, 0), errors),
                                new MockHttpServletRequest())
                        .getBody();

        assertThat(body)
                .isEqualTo(
                        new ApiErrorResponse(
                                "Validation failed",
                                400,
                                "request.validation",
                                Map.of("name", "First name message")));
    }

    @ParameterizedTest
    @MethodSource("publishedCatalogFailures")
    void mapsPublishedCatalogFailuresToStableStatusAndCode(
            RuntimeException failure, int expectedStatus, String expectedCode) {
        ApiErrorResponse body = dispatch(failure);

        assertThat(body.status()).isEqualTo(expectedStatus);
        assertThat(body.code()).isEqualTo(expectedCode);
        assertThat(body.fieldErrors()).isEmpty();
    }

    @Test
    void unexpectedFailuresNeverExposeInternals() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/admin/products/1");

        ApiErrorResponse body =
                handler.handleRuntime(
                                new IllegalStateException("SQL select leaked.SecretException"),
                                request)
                        .getBody();

        assertThat(body)
                .isEqualTo(
                        new ApiErrorResponse(
                                "An unexpected error occurred", 500, "internal.error", Map.of()));
        assertThat(body.message())
                .doesNotContain("SQL", "SecretException", "IllegalStateException");
    }

    @Test
    void responseStatusServerFailuresUseTheSameSafeGenericEnvelope() {
        ApiErrorResponse body =
                handler.handleResponseStatus(
                                new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "SQL details"),
                                new MockHttpServletRequest("GET", "/api/admin/products"))
                        .getBody();

        assertThat(body)
                .isEqualTo(
                        new ApiErrorResponse(
                                "An unexpected error occurred", 500, "internal.error", Map.of()));
    }

    private ApiErrorResponse dispatch(RuntimeException failure) {
        if (failure instanceof InvalidCatalogOperationException ex)
            return handler.handleInvalid(ex).getBody();
        if (failure instanceof ProductNotFoundException ex)
            return handler.handleProductNotFound(ex).getBody();
        if (failure instanceof CategoryNotFoundException ex)
            return handler.handleCategoryNotFound(ex).getBody();
        if (failure instanceof DuplicateSkuException ex)
            return handler.handleDuplicateSku(ex).getBody();
        if (failure instanceof DuplicateCategoryException ex)
            return handler.handleDuplicateCategory(ex).getBody();
        if (failure instanceof StaleProductRevisionException ex)
            return handler.handleStaleProduct(ex).getBody();
        if (failure instanceof StaleCategoryRevisionException ex)
            return handler.handleStaleCategory(ex).getBody();
        if (failure instanceof CategoryInUseException ex)
            return handler.handleCategoryInUse(ex).getBody();
        throw new IllegalArgumentException("Unexpected test failure");
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> publishedCatalogFailures() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        new InvalidCatalogOperationException("Invalid"), 400, "catalog.invalid"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new ProductNotFoundException(new ProductReference(1)),
                        404,
                        "catalog.product.not-found"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new CategoryNotFoundException(new CategoryReference(1)),
                        404,
                        "catalog.category.not-found"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new DuplicateSkuException("SKU"), 409, "catalog.product.sku-conflict"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new DuplicateCategoryException("Name", "slug"),
                        409,
                        "catalog.category.conflict"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new StaleProductRevisionException(
                                new ProductReference(1), new ProductRevision(0)),
                        409,
                        "catalog.product.stale"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new StaleCategoryRevisionException(
                                new CategoryReference(1), new CategoryRevision(0)),
                        409,
                        "catalog.category.stale"),
                org.junit.jupiter.params.provider.Arguments.of(
                        new CategoryInUseException(new CategoryReference(1)),
                        409,
                        "catalog.category.in-use"));
    }

    @SuppressWarnings("unused")
    private void request(Object ignored) {}
}
