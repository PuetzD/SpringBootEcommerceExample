package com.springbootecommerce.shophappens.administration.web.api;

import java.util.Map;

public record ApiErrorResponse(
        String message, int status, String code, Map<String, String> fieldErrors) {
    public ApiErrorResponse(String message, int status, String code) {
        this(message, status, code, Map.of());
    }
}
