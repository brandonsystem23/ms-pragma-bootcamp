package com.pragma.bootcamp_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CapabilityDetailResponse(
        Long id,
        String name,
        String description,
        List<TechnologyDetailResponse> technologies
) {
}
