package com.pragma.bootcamp_service.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CapabilityBasicResponse (
        Long id,
        String name,
        List<TechnologyBasicResponse> technologies
) {
}
