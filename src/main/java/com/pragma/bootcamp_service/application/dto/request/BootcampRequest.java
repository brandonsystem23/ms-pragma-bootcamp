package com.pragma.bootcamp_service.application.dto.request;

import java.time.LocalDate;
import java.util.List;

public record BootcampRequest(
        String name,
        String description,
        LocalDate launchDate,
        Integer durationDay,
        List<Long> capabilityIds
) {
}
