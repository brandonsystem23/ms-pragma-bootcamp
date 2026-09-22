package com.pragma.bootcamp_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record BootcampHistoryCreateRequest(
        Long bootcampId,
        String name,
        String description,
        LocalDate launchDate,
        Integer durationDay,
        List<BootcampHistoryCapabilityRequest> capabilities
) {
}
