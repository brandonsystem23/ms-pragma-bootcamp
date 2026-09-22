package com.pragma.bootcamp_service.infrastructure.out.webclient.dto;

import lombok.Builder;

@Builder
public record BootcampHistoryTechnologyRequest(
        String name
) {
}
