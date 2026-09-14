package com.pragma.bootcamp_service.infrastructure.input.rest;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
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
class BootcampControllerTest {

    @Mock
    private IBootcampHandler iBootcampHandler;

    @InjectMocks
    private BootcampController bootcampController;

    @Test
    void shouldCreateBootcampSuccessfully() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        BootcampRequest request = new BootcampRequest(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        BootcampResponse response = BootcampResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(request.launchDate())
                .durationDay(request.durationDay())
                .numberCapabilities(3L)
                .build();

        when(iBootcampHandler.create(request, token))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        bootcampController.createBootcamp(
                                authorizationHeader,
                                request
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iBootcampHandler).create(request, token);
    }

    @Test
    void shouldPropagateErrorWhenCreateFails() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        BootcampRequest request = new BootcampRequest(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        RuntimeException exception =
                new RuntimeException("error creando bootcamp");

        when(iBootcampHandler.create(request, token))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampController.createBootcamp(
                                authorizationHeader,
                                request
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException
                                && error.getMessage()
                                .equals("error creando bootcamp")
                )
                .verify();

        verify(iBootcampHandler).create(request, token);
    }

    @Test
    void shouldExtractTokenFromAuthorizationHeader() {

        String authorizationHeader = "Bearer abc123";
        String expectedToken = "abc123";

        BootcampRequest request = new BootcampRequest(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        BootcampResponse response = BootcampResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .launchDate(request.launchDate())
                .durationDay(request.durationDay())
                .numberCapabilities(3L)
                .build();

        when(iBootcampHandler.create(request, expectedToken))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        bootcampController.createBootcamp(
                                authorizationHeader,
                                request
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iBootcampHandler)
                .create(request, expectedToken);
    }

    @Test
    void shouldGetBootcampsSuccessfully() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "asc";

        PagedBootcampResponse response = PagedBootcampResponse.builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();

        when(iBootcampHandler.getBootcamps(
                page,
                size,
                sortBy,
                direction,
                token
        )).thenReturn(Mono.just(response));

        StepVerifier.create(
                        bootcampController.getBootcamps(
                                authorizationHeader,
                                page,
                                size,
                                sortBy,
                                direction
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iBootcampHandler).getBootcamps(
                page,
                size,
                sortBy,
                direction,
                token
        );
    }
}

