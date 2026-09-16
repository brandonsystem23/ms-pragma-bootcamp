package com.pragma.bootcamp_service.application.dto.response;

import lombok.Builder;

@Builder
public record BootcampEnrollmentResponse(
        Long bootcampId,
        Long participantId,
        String message
) {
}
