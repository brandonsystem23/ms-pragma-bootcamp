package com.pragma.bootcamp_service.domain.model;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
