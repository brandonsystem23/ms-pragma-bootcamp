package com.pragma.bootcamp_service.domain.model.command;

public record BootcampEnrollmentCommand(
        Long bootcampId,
        Long participantId
) {
}

