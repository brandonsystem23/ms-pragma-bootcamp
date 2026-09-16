package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
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
    private BootcampEnrollmentValidator bootcampEnrollmentValidator;

    @InjectMocks
    private BootcampEnrollmentUseCase bootcampEnrollmentUseCase;

    @Test
    void shouldEnrollParticipantSuccessfully() {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(1L, 100L);

        when(bootcampEnrollmentValidator.validate(1L, 100L))
                .thenReturn(Mono.empty());

        when(iBootcampParticipantPersistencePort.saveEnrollment(1L, 100L))
                .thenReturn(Mono.empty());

        StepVerifier.create(bootcampEnrollmentUseCase.enroll(command))
                .verifyComplete();

        verify(bootcampEnrollmentValidator).validate(1L, 100L);
        verify(iBootcampParticipantPersistencePort).saveEnrollment(1L, 100L);
    }

    @Test
    void shouldPropagateErrorWhenValidationFails() {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(1L, 100L);

        RuntimeException exception = new RuntimeException("error validando inscripción");

        when(bootcampEnrollmentValidator.validate(1L, 100L))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(bootcampEnrollmentUseCase.enroll(command))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando inscripción"))
                .verify();
    }
}
