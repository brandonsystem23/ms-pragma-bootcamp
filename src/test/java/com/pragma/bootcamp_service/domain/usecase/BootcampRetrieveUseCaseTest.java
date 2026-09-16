package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.DomainBootcampValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampRetrieveUseCaseTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private DomainBootcampValidator domainBootcampValidator;

    @Mock
    private CapabilityDetailService capabilityDetailService;

    @InjectMocks
    private BootcampRetrieveUseCase bootcampRetrieveUseCase;

    @Test
    void shouldGetBootcampsAndEnrichCapabilitiesSuccessfully() {

        String token = "Bearer token";

        BootcampPageCommand command = new BootcampPageCommand(
                0,
                10,
                "name",
                "asc"
        );

        Capability capability1 = Capability.builder()
                .id(1L)
                .name("Programación")
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .name("Bases de datos")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 1))
                .durationDay(30)
                .capabilities(List.of(capability1, capability2))
                .build();

        Capability enrichedCapability1 = Capability.builder()
                .id(1L)
                .name("Programación")
                .build();

        Capability enrichedCapability2 = Capability.builder()
                .id(2L)
                .name("Bases de datos")
                .build();

        List<Capability> enrichedCapabilities = List.of(
                enrichedCapability1,
                enrichedCapability2
        );

        PagedResult<Bootcamp> pagedResult = PagedResult.<Bootcamp>builder()
                .content(List.of(bootcamp))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(iBootcampPersistencePort.findAll(
                0,
                10,
                "name",
                "asc"
        )).thenReturn(Mono.just(pagedResult));

        when(capabilityDetailService.enrich(
                List.of(capability1, capability2),
                token
        )).thenReturn(Mono.just(enrichedCapabilities));

        StepVerifier.create(
                        bootcampRetrieveUseCase.getBootcamps(
                                command,
                                token
                        )
                )
                .assertNext(result -> {

                    assertEquals(1, result.content().size());
                    assertEquals(0, result.page());
                    assertEquals(10, result.size());
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                    assertTrue(result.first());
                    assertTrue(result.last());

                    Bootcamp resultBootcamp = result.content().getFirst();

                    assertEquals(1L, resultBootcamp.getId());
                    assertEquals(
                            "Desarrollo Backend",
                            resultBootcamp.getName()
                    );

                    assertEquals(
                            enrichedCapabilities,
                            resultBootcamp.getCapabilities()
                    );
                })
                .verifyComplete();

        verify(domainBootcampValidator).validatePagination(command);

        verify(iBootcampPersistencePort).findAll(
                0,
                10,
                "name",
                "asc"
        );

        verify(capabilityDetailService).enrich(
                List.of(capability1, capability2),
                token
        );

        verifyNoMoreInteractions(
                iBootcampPersistencePort,
                capabilityDetailService,
                domainBootcampValidator
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoBootcampsAreFound() {

        String token = "Bearer token";

        BootcampPageCommand command = new BootcampPageCommand(
                0,
                10,
                "name",
                "asc"
        );

        PagedResult<Bootcamp> pagedResult = PagedResult.<Bootcamp>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();

        when(iBootcampPersistencePort.findAll(
                0,
                10,
                "name",
                "asc"
        )).thenReturn(Mono.just(pagedResult));

        StepVerifier.create(
                        bootcampRetrieveUseCase.getBootcamps(
                                command,
                                token
                        )
                )
                .assertNext(result -> {
                    assertEquals(0, result.content().size());
                    assertEquals(0, result.page());
                    assertEquals(10, result.size());
                    assertEquals(0L, result.totalElements());
                    assertEquals(0, result.totalPages());
                    assertTrue(result.first());
                    assertTrue(result.last());
                })
                .verifyComplete();

        verify(domainBootcampValidator).validatePagination(command);

        verify(iBootcampPersistencePort).findAll(
                0,
                10,
                "name",
                "asc"
        );

        verifyNoInteractions(capabilityDetailService);
    }

    @Test
    void shouldPropagateErrorWhenPaginationValidationFails() {

        String token = "Bearer token";

        BootcampPageCommand command = new BootcampPageCommand(
                -1,
                10,
                "name",
                "asc"
        );

        RuntimeException exception =
                new RuntimeException("página inválida");

        doThrow(exception)
                .when(domainBootcampValidator)
                .validatePagination(command);

        StepVerifier.create(
                        bootcampRetrieveUseCase.getBootcamps(
                                command,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("página inválida")
                )
                .verify();

        verify(domainBootcampValidator)
                .validatePagination(command);

        verifyNoInteractions(
                iBootcampPersistencePort,
                capabilityDetailService
        );
    }

    @Test
    void shouldPropagateErrorWhenFindingBootcampsFails() {

        String token = "Bearer token";

        BootcampPageCommand command = new BootcampPageCommand(
                0,
                10,
                "name",
                "asc"
        );

        RuntimeException exception =
                new RuntimeException("error obteniendo bootcamps");

        when(iBootcampPersistencePort.findAll(
                0,
                10,
                "name",
                "asc"
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampRetrieveUseCase.getBootcamps(
                                command,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "error obteniendo bootcamps"
                                )
                )
                .verify();

        verify(domainBootcampValidator)
                .validatePagination(command);

        verify(iBootcampPersistencePort).findAll(
                0,
                10,
                "name",
                "asc"
        );

        verifyNoInteractions(capabilityDetailService);
    }

    @Test
    void shouldPropagateErrorWhenEnrichingCapabilitiesFails() {

        String token = "Bearer token";

        BootcampPageCommand command = new BootcampPageCommand(
                0,
                10,
                "name",
                "asc"
        );

        Capability capability = Capability.builder()
                .id(1L)
                .name("Programación")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(LocalDate.of(2026, Month.OCTOBER, 1))
                .durationDay(30)
                .capabilities(List.of(capability))
                .build();

        PagedResult<Bootcamp> pagedResult = PagedResult.<Bootcamp>builder()
                .content(List.of(bootcamp))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        RuntimeException exception =
                new RuntimeException("error enriqueciendo capacidades");

        when(iBootcampPersistencePort.findAll(
                0,
                10,
                "name",
                "asc"
        )).thenReturn(Mono.just(pagedResult));

        when(capabilityDetailService.enrich(
                List.of(capability),
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampRetrieveUseCase.getBootcamps(
                                command,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "error enriqueciendo capacidades"
                                )
                )
                .verify();

        verify(domainBootcampValidator)
                .validatePagination(command);

        verify(iBootcampPersistencePort).findAll(
                0,
                10,
                "name",
                "asc"
        );

        verify(capabilityDetailService).enrich(
                List.of(capability),
                token
        );

        verifyNoMoreInteractions(
                iBootcampPersistencePort,
                capabilityDetailService,
                domainBootcampValidator
        );
    }
}