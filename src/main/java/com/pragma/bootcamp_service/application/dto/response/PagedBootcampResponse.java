package com.pragma.bootcamp_service.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedBootcampResponse (
        List<BootcampListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
