package com.pragma.bootcamp_service.domain.validation.bootcamp;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampEnrollmentValidatorTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort;

    @InjectMocks
    private BootcampEnrollmentValidator bootcampEnrollmentValidator;

    @Test
    void shouldValidateEnrollmentSuccessfully() {
        Long bootcampId = 1L;
        Long participantId = 100L;

        Bootcamp targetBootcamp = Bootcamp.builder()
                .id(bootcampId)
                .name("Bootcamp Java")
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 10))
                .durationDay(10)
                .status(true)
                .build();

        Bootcamp otherBootcamp = Bootcamp.builder()
                .id(2L)
                .name("Bootcamp Python")
                .launchDate(LocalDate.of(2026, Month.NOVEMBER, 1))
                .durationDay(5)
                .status(true)
                .build();

        when(iBootcampPersistencePort.findActiveById(bootcampId))
                .thenReturn(Mono.just(targetBootcamp));
        when(iBootcampParticipantPersistencePort.existsByBootcampIdAndParticipantId(bootcampId, participantId))
                .thenReturn(Mono.just(false));
        when(iBootcampParticipantPersistencePort.countActiveBootcampsByParticipantId(participantId))
                .thenReturn(Mono.just(2L));
        when(iBootcampParticipantPersistencePort.findActiveBootcampsByParticipantId(participantId))
                .thenReturn(Flux.just(otherBootcamp));

        StepVerifier.create(bootcampEnrollmentValidator.validate(bootcampId, participantId))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenBootcampIdIsNull() {
        StepVerifier.create(bootcampEnrollmentValidator.validate(null, 100L))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_ID_REQUIRED))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenParticipantIdIsNull() {
        StepVerifier.create(bootcampEnrollmentValidator.validate(1L, null))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.PARTICIPANT_ID_REQUIRED))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenBootcampDoesNotExist() {
        when(iBootcampPersistencePort.findActiveById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(bootcampEnrollmentValidator.validate(1L, 100L))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.BOOTCAMP_NOT_FOUND &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_NOT_FOUND))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenParticipantAlreadyEnrolled() {
        Bootcamp targetBootcamp = Bootcamp.builder()
                .id(1L)
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 10))
                .durationDay(10)
                .status(true)
                .build();

        when(iBootcampPersistencePort.findActiveById(1L))
                .thenReturn(Mono.just(targetBootcamp));
        when(iBootcampParticipantPersistencePort.existsByBootcampIdAndParticipantId(1L, 100L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(bootcampEnrollmentValidator.validate(1L, 100L))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.PARTICIPANT_ALREADY_ENROLLED &&
                                error.getMessage().equals(DomainErrorMessages.PARTICIPANT_ALREADY_ENROLLED))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenParticipantHasReachedMaxBootcamps() {
        Bootcamp targetBootcamp = Bootcamp.builder()
                .id(1L)
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 10))
                .durationDay(10)
                .status(true)
                .build();

        when(iBootcampPersistencePort.findActiveById(1L))
                .thenReturn(Mono.just(targetBootcamp));
        when(iBootcampParticipantPersistencePort.existsByBootcampIdAndParticipantId(1L, 100L))
                .thenReturn(Mono.just(false));
        when(iBootcampParticipantPersistencePort.countActiveBootcampsByParticipantId(100L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(bootcampEnrollmentValidator.validate(1L, 100L))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.MAX_ACTIVE_BOOTCAMPS_REACHED &&
                                error.getMessage().equals(DomainErrorMessages.MAX_ACTIVE_BOOTCAMPS_REACHED))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenScheduleConflictExists() {
        Long bootcampId = 1L;
        Long participantId = 100L;

        Bootcamp targetBootcamp = Bootcamp.builder()
                .id(bootcampId)
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 10))
                .durationDay(10)
                .status(true)
                .build();

        Bootcamp conflictingBootcamp = Bootcamp.builder()
                .id(2L)
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 15))
                .durationDay(7)
                .status(true)
                .build();

        when(iBootcampPersistencePort.findActiveById(bootcampId))
                .thenReturn(Mono.just(targetBootcamp));
        when(iBootcampParticipantPersistencePort.existsByBootcampIdAndParticipantId(bootcampId, participantId))
                .thenReturn(Mono.just(false));
        when(iBootcampParticipantPersistencePort.countActiveBootcampsByParticipantId(participantId))
                .thenReturn(Mono.just(2L));
        when(iBootcampParticipantPersistencePort.findActiveBootcampsByParticipantId(participantId))
                .thenReturn(Flux.just(conflictingBootcamp));

        StepVerifier.create(bootcampEnrollmentValidator.validate(bootcampId, participantId))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.BOOTCAMP_SCHEDULE_CONFLICT &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_SCHEDULE_CONFLICT))
                .verify();
    }
}
