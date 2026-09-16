package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampEnrollmentServicePort;
import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampEnrollmentValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampEnrollmentUseCase implements IBootcampEnrollmentServicePort {

    private final IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort;
    private final BootcampEnrollmentValidator bootcampEnrollmentValidator;

    @Override
    public Mono<Void> enroll(BootcampEnrollmentCommand command) {

        return bootcampEnrollmentValidator.validate(command.bootcampId(), command.participantId())
                        .then(Mono.defer(() ->
                                iBootcampParticipantPersistencePort.saveEnrollment(
                                        command.bootcampId(),
                                        command.participantId())
                                )
                        );


    }
}
