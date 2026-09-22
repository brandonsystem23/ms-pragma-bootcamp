package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IReportWebClientPort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampValidator;
import com.pragma.bootcamp_service.domain.validation.bootcamp.DomainBootcampValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampRegisterUseCaseTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private IReportWebClientPort iReportWebClientPort;

    @Mock
    private DomainBootcampValidator domainBootcampValidator;

    @Mock
    private BootcampValidator bootcampValidator;

    @Mock
    private CapabilityDetailService capabilityDetailService;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private BootcampRegisterUseCase bootcampRegisterUseCase;

    @Test
    void shouldCreateBootcampSuccessfully() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        List<Capability> savedCapabilities = List.of(
                Capability.builder().id(1L).build(),
                Capability.builder().id(2L).build(),
                Capability.builder().id(3L).build()
        );

        List<Capability> enrichedCapabilities = List.of(
                Capability.builder().id(1L).name("Programación").build(),
                Capability.builder().id(2L).name("Bases de datos").build(),
                Capability.builder().id(3L).name("Java").build()
        );

        Bootcamp savedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(launchDate)
                .durationDay(30)
                .capabilities(savedCapabilities)
                .build();

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.empty());

        when(iBootcampPersistencePort.save(
                ArgumentMatchers.any(Bootcamp.class)
        )).thenReturn(Mono.just(savedBootcamp));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(capabilityDetailService.enrich(savedCapabilities, token))
                .thenReturn(Mono.just(enrichedCapabilities));

        when(iReportWebClientPort.createBootcampHistory(any(Bootcamp.class), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1L, result.getId());
                    org.junit.jupiter.api.Assertions.assertEquals("Desarrollo Backend", result.getName());
                    org.junit.jupiter.api.Assertions.assertEquals(enrichedCapabilities, result.getCapabilities());
                })
                .verifyComplete();

        verify(iReportWebClientPort).createBootcampHistory(any(Bootcamp.class), any(String.class));
    }

    @Test
    void shouldContinueWhenEnrichBootcampFailsAndStillCreateHistory() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        List<Capability> savedCapabilities = List.of(
                Capability.builder().id(1L).build(),
                Capability.builder().id(2L).build()
        );

        Bootcamp savedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(launchDate)
                .durationDay(30)
                .capabilities(savedCapabilities)
                .build();

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.empty());

        when(iBootcampPersistencePort.save(any(Bootcamp.class)))
                .thenReturn(Mono.just(savedBootcamp));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(capabilityDetailService.enrich(savedCapabilities, token))
                .thenReturn(Mono.error(new RuntimeException("error enriqueciendo bootcamp")));

        when(iReportWebClientPort.createBootcampHistory(savedBootcamp, token))
                .thenReturn(Mono.empty());

        StepVerifier.create(bootcampRegisterUseCase.create(command, token))
                .expectNext(savedBootcamp)
                .verifyComplete();

        verify(iReportWebClientPort).createBootcampHistory(savedBootcamp, token);
    }

    @Test
    void shouldReturnDomainExceptionWhenCreateBootcampHistoryFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        List<Capability> savedCapabilities = List.of(
                Capability.builder().id(1L).build(),
                Capability.builder().id(2L).build()
        );

        List<Capability> enrichedCapabilities = List.of(
                Capability.builder().id(1L).name("Programación").build(),
                Capability.builder().id(2L).name("Bases de datos").build()
        );

        Bootcamp savedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(launchDate)
                .durationDay(30)
                .capabilities(savedCapabilities)
                .build();

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.empty());

        when(iBootcampPersistencePort.save(any(Bootcamp.class)))
                .thenReturn(Mono.just(savedBootcamp));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(capabilityDetailService.enrich(savedCapabilities, token))
                .thenReturn(Mono.just(enrichedCapabilities));

        when(iReportWebClientPort.createBootcampHistory(any(Bootcamp.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("error report")));

        StepVerifier.create(bootcampRegisterUseCase.create(command, token))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_HISTORY_SAVE_ERROR)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenDomainValidationFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

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

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_SAVE_ROLLBACK_ERROR)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenBootcampValidationFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

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

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_SAVE_ROLLBACK_ERROR)
                )
                .verify();

    }

    @Test
    void shouldPropagateErrorWhenSaveFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

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

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_SAVE_ROLLBACK_ERROR)
                )
                .verify();
    }

    @Test
    void shouldPropagateDomainExceptionWithoutMapping() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        DomainException domainException = new DomainException(
                DomainErrorCode.INTERNAL_ERROR,
                DomainErrorMessages.BOOTCAMP_SAVE_ROLLBACK_ERROR
        );

        when(bootcampValidator.validateBootcamp(
                command.name(),
                command.capabilityIds(),
                token
        )).thenReturn(Mono.error(domainException));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampRegisterUseCase.create(command, token)
                )
                .expectErrorSatisfies(error -> assertSame(domainException, error))
                .verify();
    }
}
