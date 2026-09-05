package com.springbootecommerce.shophappens.administration.web;

import com.springbootecommerce.shophappens.administration.web.api.ApiErrorResponse;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCategoryException;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleCategoryRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.customer.application.CustomerNotFoundException;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class AdminApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(
                        error ->
                                fieldErrors.putIfAbsent(
                                        error.getField(), error.getDefaultMessage()));
        ApiErrorResponse body =
                new ApiErrorResponse(
                        "Validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        "request.validation",
                        fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode() != null ? ex.getStatusCode().value() : 500;
        String message =
                ex.getReason() != null && !ex.getReason().isBlank()
                        ? ex.getReason()
                        : userMessage(status);
        String code = status == 400 ? "catalog.invalid" : "internal.error";
        ApiErrorResponse body = new ApiErrorResponse(message, status, code);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({InvalidCatalogOperationException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalid(RuntimeException ex) {
        return response(ex.getMessage(), HttpStatus.BAD_REQUEST, "catalog.invalid");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return response(ex.getMessage(), HttpStatus.BAD_REQUEST, "catalog.invalid");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, "catalog.product.not-found");
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, "catalog.category.not-found");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, "ordering.order.not-found");
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        return response(ex.getMessage(), HttpStatus.NOT_FOUND, "customer.not-found");
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSku(DuplicateSkuException ex) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, "catalog.product.sku-conflict");
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateCategory(DuplicateCategoryException ex) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, "catalog.category.conflict");
    }

    @ExceptionHandler(StaleProductRevisionException.class)
    public ResponseEntity<ApiErrorResponse> handleStaleProduct(StaleProductRevisionException ex) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, "catalog.product.stale");
    }

    @ExceptionHandler(StaleCategoryRevisionException.class)
    public ResponseEntity<ApiErrorResponse> handleStaleCategory(StaleCategoryRevisionException ex) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, "catalog.category.stale");
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryInUse(CategoryInUseException ex) {
        return response(ex.getMessage(), HttpStatus.CONFLICT, "catalog.category.in-use");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(
            RuntimeException ex, HttpServletRequest request) {
        LOGGER.error(
                "Unexpected admin API failure for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);
        ApiErrorResponse body =
                new ApiErrorResponse(
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "internal.error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ApiErrorResponse> response(
            String message, HttpStatus status, String code) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(message, status.value(), code));
    }

    private String userMessage(int status) {
        return switch (status) {
            case 400 -> "Bad request";
            case 403 -> "Access denied";
            case 404 -> "Resource not found";
            case 409 -> "Conflict";
            default -> "An error occurred";
        };
    }
}
