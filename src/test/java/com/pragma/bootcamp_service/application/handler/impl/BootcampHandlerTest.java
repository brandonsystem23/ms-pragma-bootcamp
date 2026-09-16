package com.pragma.bootcamp_service.application.handler.impl;

import com.pragma.bootcamp_service.application.dto.request.BootcampEnrollmentRequest;
import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampListItemResponse;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.CapabilityBasicResponse;
import com.pragma.bootcamp_service.application.dto.response.TechnologyBasicResponse;
import com.pragma.bootcamp_service.application.mapper.BootcampDtoMapper;
import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampEnrollmentServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampHandlerTest {

    @Mock
    private IBootcampRegisterServicePort iBootcampRegisterServicePort;

    @Mock
    private IBootcampRetrieveServicePort iBootcampRetrieveServicePort;

    @Mock
    private IBootcampDeleteServicePort iBootcampDeleteServicePort;

    @Mock
    private BootcampDtoMapper bootcampDtoMapper;

    @Mock
    private IBootcampEnrollmentServicePort iBootcampEnrollmentServicePort;

    @InjectMocks
    private BootcampHandler bootcampHandler;

    @Test
    void shouldCreateBootcampAndMapResponse() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampRequest request = new BootcampRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .launchDate(launchDate)
                .durationDay(30)
                .capabilities(List.of())
                .build();

        BootcampResponse response = BootcampResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .launchDate(launchDate)
                .durationDay(30)
                .numberCapabilities(3L)
                .build();

        when(bootcampDtoMapper.toCommand(request))
                .thenReturn(command);

        when(iBootcampRegisterServicePort.create(command, token))
                .thenReturn(Mono.just(bootcamp));

        when(bootcampDtoMapper.toResponse(bootcamp))
                .thenReturn(response);

        StepVerifier.create(bootcampHandler.create(request, token))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenCreateFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        BootcampRequest request = new BootcampRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                launchDate,
                30,
                List.of(1L, 2L, 3L)
        );

        RuntimeException exception =
                new RuntimeException("error creando bootcamp");

        when(bootcampDtoMapper.toCommand(request))
                .thenReturn(command);

        when(iBootcampRegisterServicePort.create(command, token))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(bootcampHandler.create(request, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error creando bootcamp")
                )
                .verify();

    }

    @Test
    void shouldGetBootcampsAndMapResponse() {

        String token = "Bearer token";

        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "asc";

        BootcampPageCommand command = new BootcampPageCommand(
                page,
                size,
                sortBy,
                direction
        );

        LocalDate launchDate = LocalDate.of(2026, Month.OCTOBER, 1);

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .launchDate(launchDate)
                .durationDay(30)
                .capabilities(List.of())
                .build();

        TechnologyBasicResponse technology = TechnologyBasicResponse.builder()
                .id(1L)
                .name("Java")
                .build();

        CapabilityBasicResponse capability = CapabilityBasicResponse.builder()
                .id(1L)
                .name("Programación")
                .technologies(List.of(technology))
                .build();

        BootcampListItemResponse listItemResponse =
                BootcampListItemResponse.builder()
                        .id(1L)
                        .name("Desarrollo Backend")
                        .description("Capacidad para desarrollar servicios backend")
                        .launchDate(launchDate)
                        .durationDay(30)
                        .capabilities(List.of(capability))
                        .build();

        PagedResult<Bootcamp> result = new PagedResult<>(
                List.of(bootcamp),
                page,
                size,
                1L,
                1,
                true,
                true
        );

        when(iBootcampRetrieveServicePort.getBootcamps(
                command,
                token
        )).thenReturn(Mono.just(result));

        when(bootcampDtoMapper.toListItemResponse(bootcamp))
                .thenReturn(listItemResponse);

        StepVerifier.create(
                        bootcampHandler.getBootcamps(
                                page,
                                size,
                                sortBy,
                                direction,
                                token
                        )
                )
                .assertNext(response -> {

                    assertEquals(1, response.content().size());

                    assertEquals(
                            listItemResponse,
                            response.content().getFirst()
                    );

                    assertEquals(
                            1L,
                            response.content().getFirst().id()
                    );

                    assertEquals(
                            "Desarrollo Backend",
                            response.content().getFirst().name()
                    );

                    assertEquals(
                            1,
                            response.content().getFirst().capabilities().size()
                    );

                    assertEquals(
                            "Programación",
                            response.content()
                                    .getFirst()
                                    .capabilities()
                                    .getFirst()
                                    .name()
                    );

                    assertEquals(
                            1,
                            response.content()
                                    .getFirst()
                                    .capabilities()
                                    .getFirst()
                                    .technologies()
                                    .size()
                    );

                    assertEquals(
                            "Java",
                            response.content()
                                    .getFirst()
                                    .capabilities()
                                    .getFirst()
                                    .technologies()
                                    .getFirst()
                                    .name()
                    );

                    assertEquals(page, response.page());
                    assertEquals(size, response.size());
                    assertEquals(1L, response.totalElements());
                    assertEquals(1, response.totalPages());
                    assertTrue(response.first());
                    assertTrue(response.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenGetBootcampsFails() {

        String token = "Bearer token";

        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "asc";

        BootcampPageCommand command = new BootcampPageCommand(
                page,
                size,
                sortBy,
                direction
        );

        RuntimeException exception =
                new RuntimeException("error obteniendo bootcamps");

        when(iBootcampRetrieveServicePort.getBootcamps(
                command,
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampHandler.getBootcamps(
                                page,
                                size,
                                sortBy,
                                direction,
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

    }

    @Test
    void shouldDeleteBootcampByIdSuccessfully() {
        Long bootcampId = 1L;
        String token = "token";

        when(iBootcampDeleteServicePort.deleteById(bootcampId, token))
                .thenReturn(Mono.empty());

        StepVerifier.create(bootcampHandler.deleteById(bootcampId, token))
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenDeleteBootcampByIdFails() {
        Long bootcampId = 1L;
        String token = "token";

        when(iBootcampDeleteServicePort.deleteById(bootcampId, token))
                .thenReturn(Mono.error(new RuntimeException("error eliminando bootcamp")));

        StepVerifier.create(bootcampHandler.deleteById(bootcampId, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error eliminando bootcamp"))
                .verify();
    }

    @Test
    void shouldEnrollToBootcampSuccessfully() {

        Long bootcampId = 1L;
        Long participantId = 10L;

        BootcampEnrollmentRequest request =
                new BootcampEnrollmentRequest(bootcampId);

        BootcampEnrollmentCommand command =
                new BootcampEnrollmentCommand(bootcampId, participantId);

        when(iBootcampEnrollmentServicePort.enroll(command))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        bootcampHandler.enroll(request, participantId)
                )
                .assertNext(response -> {
                    assertEquals(bootcampId, response.bootcampId());
                    assertEquals(participantId, response.participantId());
                    assertEquals(
                            "Inscripción realizada exitosamente",
                            response.message()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenEnrollmentFails() {

        Long bootcampId = 1L;
        Long participantId = 10L;

        BootcampEnrollmentRequest request =
                new BootcampEnrollmentRequest(bootcampId);

        BootcampEnrollmentCommand command =
                new BootcampEnrollmentCommand(bootcampId, participantId);

        RuntimeException exception =
                new RuntimeException("error realizando inscripción");

        when(iBootcampEnrollmentServicePort.enroll(command))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampHandler.enroll(request, participantId)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "error realizando inscripción"
                                )
                )
                .verify();
    }

}
