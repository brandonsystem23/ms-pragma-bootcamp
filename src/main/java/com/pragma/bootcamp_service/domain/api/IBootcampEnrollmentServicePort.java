package com.pragma.bootcamp_service.domain.api;

import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import reactor.core.publisher.Mono;

public interface IBootcampEnrollmentServicePort {

    Mono<Void> enroll(BootcampEnrollmentCommand command, String fullName, String email, String token);
}
