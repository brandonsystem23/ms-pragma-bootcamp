package com.pragma.bootcamp_service.domain.validation.bootcamp;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
public class BootcampEnrollmentValidator {

    private static final long MAX_ACTIVE_BOOTCAMPS = 5L;

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort;


    public Mono<Void> validate(Long bootcampId, Long participantId) {
        if (bootcampId == null) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.BOOTCAMP_ID_REQUIRED
            ));
        }

        if (participantId == null) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.PARTICIPANT_ID_REQUIRED
            ));
        }

        return iBootcampPersistencePort.findActiveById(bootcampId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.BOOTCAMP_NOT_FOUND,
                        DomainErrorMessages.BOOTCAMP_NOT_FOUND
                )))
                .flatMap(targetBootcamp ->
                        validateAlreadyEnrolled(bootcampId, participantId)
                                .then(Mono.defer(() -> validateMaxActiveBootcamps(participantId)))
                                .then(Mono.defer(() -> validateScheduleConflict(participantId, targetBootcamp)))
                );
    }

    private Mono<Void> validateAlreadyEnrolled(Long bootcampId, Long participantId) {
        return iBootcampParticipantPersistencePort.existsByBootcampIdAndParticipantId(bootcampId, participantId)
                .flatMap(exists ->
                        Boolean.TRUE.equals(exists)
                                ? Mono.error(new DomainException(
                                DomainErrorCode.PARTICIPANT_ALREADY_ENROLLED,
                                DomainErrorMessages.PARTICIPANT_ALREADY_ENROLLED
                        )) : Mono.empty()
                );
    }

    private Mono<Void> validateMaxActiveBootcamps(Long participantId) {
        return iBootcampParticipantPersistencePort.countActiveBootcampsByParticipantId(participantId)
                .flatMap(count ->
                        count >= MAX_ACTIVE_BOOTCAMPS
                                ? Mono.error(new DomainException(
                                DomainErrorCode.MAX_ACTIVE_BOOTCAMPS_REACHED,
                                DomainErrorMessages.MAX_ACTIVE_BOOTCAMPS_REACHED
                        )) : Mono.empty()
                );
    }

    private Mono<Void> validateScheduleConflict(Long participantId, Bootcamp targetBootcamp) {
        return iBootcampParticipantPersistencePort.findActiveBootcampsByParticipantId(participantId)
                .any(enrolledBootcamp -> hasDateConflict(enrolledBootcamp, targetBootcamp))
                .flatMap(conflict ->
                        Boolean.TRUE.equals(conflict)
                                ? Mono.error(new DomainException(
                                DomainErrorCode.BOOTCAMP_SCHEDULE_CONFLICT,
                                DomainErrorMessages.BOOTCAMP_SCHEDULE_CONFLICT
                        )) : Mono.empty()
                );
    }

    private boolean hasDateConflict(Bootcamp current, Bootcamp target) {
        LocalDate currentStart = current.getLaunchDate();
        LocalDate currentEnd = currentStart.plusDays(current.getDurationDay() - 1L);

        LocalDate targetStart = target.getLaunchDate();
        LocalDate targetEnd = targetStart.plusDays(target.getDurationDay() - 1L);

        return !targetEnd.isBefore(currentStart) && !targetStart.isAfter(currentEnd);
    }
}
