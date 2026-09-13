package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.validation.capability.BootcampValidator;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampRegisterUseCaseTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private DomainBootcampValidator domainBootcampValidator;

    @Mock
    private BootcampValidator bootcampValidator;

    @InjectMocks
    private BootcampRegisterUseCase bootcampRegisterUseCase;

    @Test
    void shouldCreateBootcampSuccessfully() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        Bootcamp savedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(launchDate)
                .durationDay(30)
                .build();

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.empty());

        when(iBootcampPersistencePort.save(
                ArgumentMatchers.any(Bootcamp.class)
        )).thenReturn(Mono.just(savedBootcamp));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectNext(savedBootcamp)
                .verifyComplete();

        verify(domainBootcampValidator)
                .validateBootcampCommand(command);

        verify(bootcampValidator)
                .validateBootcamp(
                        command.name(),
                        command.capabilityIds(),
                        token
                );

        verify(iBootcampPersistencePort)
                .save(ArgumentMatchers.any(Bootcamp.class));
    }

    @Test
    void shouldPropagateErrorWhenDomainValidationFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        RuntimeException exception =
                new RuntimeException("error validando comando");

        doThrow(exception)
                .when(domainBootcampValidator)
                .validateBootcampCommand(command);

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando comando")
                )
                .verify();

        verify(domainBootcampValidator)
                .validateBootcampCommand(command);

        verifyNoInteractions(bootcampValidator);
        verifyNoInteractions(iBootcampPersistencePort);
    }

    @Test
    void shouldPropagateErrorWhenBootcampValidationFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        RuntimeException exception =
                new RuntimeException("error validando capacidades");

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando capacidades")
                )
                .verify();

        verify(domainBootcampValidator)
                .validateBootcampCommand(command);

        verify(bootcampValidator)
                .validateBootcamp(
                        command.name(),
                        command.capabilityIds(),
                        token
                );

        verifyNoInteractions(iBootcampPersistencePort);
    }

    @Test
    void shouldPropagateErrorWhenSaveFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        RuntimeException exception =
                new RuntimeException("error guardando bootcamp");

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.empty());

        when(iBootcampPersistencePort.save(
                ArgumentMatchers.any(Bootcamp.class)
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error guardando bootcamp")
                )
                .verify();

        verify(domainBootcampValidator)
                .validateBootcampCommand(command);

        verify(bootcampValidator)
                .validateBootcamp(
                        command.name(),
                        command.capabilityIds(),
                        token
                );

        verify(iBootcampPersistencePort)
                .save(ArgumentMatchers.any(Bootcamp.class));
    }
}

