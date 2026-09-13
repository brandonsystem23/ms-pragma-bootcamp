package com.pragma.bootcamp_service.application.handler.impl;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.mapper.BootcampDtoMapper;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampHandlerTest {

    @Mock
    private IBootcampRegisterServicePort iBootcampRegisterServicePort;

    @Mock
    private BootcampDtoMapper bootcampDtoMapper;

    @InjectMocks
    private BootcampHandler bootcampHandler;

    @Test
    void shouldCreateBootcampAndMapResponse() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

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

        verify(bootcampDtoMapper).toCommand(request);
        verify(iBootcampRegisterServicePort).create(command, token);
        verify(bootcampDtoMapper).toResponse(bootcamp);
    }

    @Test
    void shouldPropagateErrorWhenCreateFails() {

        String token = "Bearer token";

        LocalDate launchDate = LocalDate.of(2026, 10, 1);

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

        verify(bootcampDtoMapper).toCommand(request);
        verify(iBootcampRegisterServicePort).create(command, token);
    }
}

