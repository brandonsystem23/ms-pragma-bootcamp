package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampEnrollmentServicePort;
import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.model.dto.ParticipantBootcampHistory;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IReportWebClientPort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampEnrollmentValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampEnrollmentUseCase implements IBootcampEnrollmentServicePort {

    private final IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort;
    private final IReportWebClientPort iReportWebClientPort;
    private final BootcampEnrollmentValidator bootcampEnrollmentValidator;

    @Override
    public Mono<Void> enroll(BootcampEnrollmentCommand command, String fullName, String email, String token) {

        return bootcampEnrollmentValidator.validate(command.bootcampId(), command.participantId())
                .then(Mono.defer(() -> {

                    ParticipantBootcampHistory bootcampHistory = new ParticipantBootcampHistory(fullName, email);

                    return iBootcampParticipantPersistencePort.saveEnrollment(
                                    command.bootcampId(),
                                    command.participantId())
                            .then(Mono.defer(() ->
                                    iReportWebClientPort.updateBootcampHistoryParticipant(
                                            bootcampHistory,
                                            command.bootcampId(),
                                            token)
                                            .onErrorMap(error ->
                                                    new DomainException(
                                                            DomainErrorCode.INTERNAL_ERROR,
                                                            DomainErrorMessages.BOOTCAMP_HISTORY_UPDATE_ERROR
                                            )
                                    )
                            ));
                }));
    }
}
