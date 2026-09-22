package com.pragma.bootcamp_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BootcampHistoryCapabilityRequest(
        String name,
        List<BootcampHistoryTechnologyRequest> technologies
) {
}
