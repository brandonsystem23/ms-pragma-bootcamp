package com.pragma.bootcamp_service.application.dto.request;

public record BootcampFilterDto(
        int page,
        int size,
        String sortBy,
        String direction
) {
}
