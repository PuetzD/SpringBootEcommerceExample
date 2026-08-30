package com.springbootecommerce.shophappens.administration.web.api;

import java.util.List;

public record ApiErrorResponse(String message, int status, List<FieldErrorResponse> fieldErrors) {
    public ApiErrorResponse(String message, int status) {
        this(message, status, List.of());
    }
}
