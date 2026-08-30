package com.springbootecommerce.shophappens.administration.web;

import com.springbootecommerce.shophappens.administration.web.api.ApiErrorResponse;
import com.springbootecommerce.shophappens.administration.web.api.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class AdminApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorResponse> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()))
                        .collect(Collectors.toList());
        ApiErrorResponse body =
                new ApiErrorResponse(
                        "Validation failed", HttpStatus.BAD_REQUEST.value(), fieldErrors);
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
        ApiErrorResponse body = new ApiErrorResponse(message, status);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(
            RuntimeException ex, HttpServletRequest request) {
        ApiErrorResponse body =
                new ApiErrorResponse(
                        "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
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
