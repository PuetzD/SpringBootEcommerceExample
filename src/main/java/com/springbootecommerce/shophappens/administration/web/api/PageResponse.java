package com.springbootecommerce.shophappens.administration.web.api;

import java.util.List;

public record PageResponse<T>(
        List<T> content, int page, int size, long totalElements, int totalPages) {}
