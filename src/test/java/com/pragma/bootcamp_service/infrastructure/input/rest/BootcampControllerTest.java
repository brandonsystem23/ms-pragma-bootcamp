package com.pragma.bootcamp_service.infrastructure.input.rest;

import com.pragma.bootcamp_service.application.dto.request.BootcampEnrollmentRequest;
import com.pragma.bootcamp_service.application.dto.request.BootcampFilterDto;
import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampEnrollmentResponse;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
import com.pragma.bootcamp_service.infrastructure.security.jwt.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
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
    }

    @Test
    void shouldGetBootcampsSuccessfully() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        int page = 0;
        int size = 10;

        PagedBootcampResponse response = PagedBootcampResponse.builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();

        BootcampFilterDto bootcampFilterDto = new BootcampFilterDto(0, 10, "name", "asc");

        when(iBootcampHandler.getBootcamps(
                bootcampFilterDto,
                token
        )).thenReturn(Mono.just(response));



        when(iBootcampHandler.getBootcamps(bootcampFilterDto, token))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        bootcampController.getBootcamps(
                                authorizationHeader,
                                bootcampFilterDto
                        )
                )
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldDeleteBootcampByIdSuccessfully() {
        String authorizationHeader = "Bearer token";
        String token = "token";
        Long id = 1L;

        when(iBootcampHandler.deleteById(id, token))
                .thenReturn(Mono.empty());

        StepVerifier.create(bootcampController.deleteBootcampById(authorizationHeader, id))
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenDeleteBootcampByIdFails() {
        String authorizationHeader = "Bearer token";
        String token = "token";
        Long id = 1L;

        when(iBootcampHandler.deleteById(id, token))
                .thenReturn(Mono.error(new RuntimeException("error eliminando bootcamp")));

        StepVerifier.create(bootcampController.deleteBootcampById(authorizationHeader, id))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error eliminando bootcamp"))
                .verify();

    }

    @Test
    void shouldEnrollToBootcampSuccessfully() {

        BootcampEnrollmentRequest request = new BootcampEnrollmentRequest(
                1L
        );

        BootcampEnrollmentResponse response = BootcampEnrollmentResponse.builder()
                .bootcampId(1L)
                .participantId(10L)
                .build();

        Long participantId = 10L;

        Authentication authentication = mock(Authentication.class);
        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);

        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(authenticatedUser.userId()).thenReturn(participantId);

        when(iBootcampHandler.enroll(request, participantId))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        bootcampController.enrollToBootcamp(
                                request,
                                authentication
                        )
                )
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenEnrollFails() {

        BootcampEnrollmentRequest request = new BootcampEnrollmentRequest(1L);

        Long participantId = 10L;

        Authentication authentication = mock(Authentication.class);
        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);

        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(authenticatedUser.userId()).thenReturn(participantId);

        RuntimeException exception =
                new RuntimeException("error inscribiendo al participante");

        when(iBootcampHandler.enroll(request, participantId))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampController.enrollToBootcamp(
                                request,
                                authentication
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("error inscribiendo al participante")
                )
                .verify();
    }
}
