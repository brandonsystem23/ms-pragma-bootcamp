package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IReportWebClientPort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampEnrollmentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampEnrollmentUseCaseTest {

    @Mock
    private IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort;

    @Mock
    private IReportWebClientPort iReportWebClientPort;

    @Mock
    private BootcampEnrollmentValidator bootcampEnrollmentValidator;

    @InjectMocks
    private BootcampEnrollmentUseCase bootcampEnrollmentUseCase;

    @Test
    void shouldEnrollParticipantSuccessfully() {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(1L, 100L);
        String fullName = "Juan Perez";
        String email = "juan@test.com";
        String token = "token";

        when(bootcampEnrollmentValidator.validate(1L, 100L))
                .thenReturn(Mono.empty());

        when(iBootcampParticipantPersistencePort.saveEnrollment(1L, 100L))
                .thenReturn(Mono.empty());

        when(iReportWebClientPort.updateBootcampHistoryParticipant(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(token)
        )).thenReturn(Mono.empty());

        StepVerifier.create(bootcampEnrollmentUseCase.enroll(command, fullName, email, token))
                .verifyComplete();

        verify(bootcampEnrollmentValidator).validate(1L, 100L);
        verify(iBootcampParticipantPersistencePort).saveEnrollment(1L, 100L);
        verify(iReportWebClientPort).updateBootcampHistoryParticipant(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(token)
        );
    }

    @Test
    void shouldPropagateErrorWhenValidationFails() {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(1L, 100L);

        RuntimeException exception = new RuntimeException("error validando inscripción");

        when(bootcampEnrollmentValidator.validate(1L, 100L))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(bootcampEnrollmentUseCase.enroll(command, "Juan Perez", "juan@test.com", "token"))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando inscripción"))
                .verify();
    }

    @Test
    void shouldReturnDomainExceptionWhenUpdatingBootcampHistoryFails() {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(1L, 100L);
        String fullName = "Juan Perez";
        String email = "juan@test.com";
        String token = "token";

        when(bootcampEnrollmentValidator.validate(1L, 100L))
                .thenReturn(Mono.empty());

        when(iBootcampParticipantPersistencePort.saveEnrollment(1L, 100L))
                .thenReturn(Mono.empty());

        when(iReportWebClientPort.updateBootcampHistoryParticipant(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(token)
        )).thenReturn(Mono.error(new RuntimeException("error report")));

        StepVerifier.create(bootcampEnrollmentUseCase.enroll(command, fullName, email, token))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_HISTORY_UPDATE_ERROR))
                .verify();
    }
}
