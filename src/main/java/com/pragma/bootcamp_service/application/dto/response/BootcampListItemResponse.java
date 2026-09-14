package com.pragma.bootcamp_service.application.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record BootcampListItemResponse (
        Long id,
        String name,
        String description,
        LocalDate launchDate,
        Integer durationDay,
        List<CapabilityBasicResponse> capabilities
) {
}
