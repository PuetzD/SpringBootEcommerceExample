package com.springbootecommerce.shophappens.administration.web.api;

public record PageResponse<T>(
        T content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
