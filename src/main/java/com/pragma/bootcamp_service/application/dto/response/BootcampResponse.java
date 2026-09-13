package com.pragma.bootcamp_service.application.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BootcampResponse(

        Long id,

        String name,

        String description,

        LocalDate launchDate,

        Integer durationDay,

        Long numberCapabilities
) {
}
