package com.pragma.bootcamp_service.infrastructure.out.webclient.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.Technology;
import com.pragma.bootcamp_service.domain.model.dto.ParticipantBootcampHistory;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryCreateRequest;
import com.pragma.bootcamp_service.infrastructure.out.webclient.mapper.ReportMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BootcampHistoryWebClientAdapterTest {

    private MockWebServer mockWebServer;
    private BootcampHistoryWebClientAdapter bootcampHistoryWebClientAdapter;
    private ReportMapper reportMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient reportWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        reportMapper = mock(ReportMapper.class);

        bootcampHistoryWebClientAdapter = new BootcampHistoryWebClientAdapter(
                reportWebClient,
                "/api/v1/report/bootcamp-history/create",
                "/api/v1/report/bootcamp-history/update",
                reportMapper
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCreateBootcampHistorySuccessfully() {
        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 1))
                .durationDay(30)
                .capabilities(List.of(
                        Capability.builder()
                                .id(1L)
                                .name("Programación")
                                .technologies(List.of(
                                        Technology.builder().id(1L).name("Java").build()
                                ))
                                .build()
                ))
                .build();

        BootcampHistoryCreateRequest request = BootcampHistoryCreateRequest.builder()
                .bootcampId(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 1))
                .durationDay(30)
                .capabilities(List.of())
                .build();

        when(reportMapper.toRequest(bootcamp)).thenReturn(request);

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(bootcampHistoryWebClientAdapter.createBootcampHistory(bootcamp, "token"))
                .verifyComplete();
    }

    @Test
    void shouldSendCorrectRequestWhenCreatingBootcampHistory() throws InterruptedException {
        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 1))
                .durationDay(30)
                .capabilities(List.of())
                .build();

        BootcampHistoryCreateRequest request = BootcampHistoryCreateRequest.builder()
                .bootcampId(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 1))
                .durationDay(30)
                .capabilities(List.of())
                .build();

        when(reportMapper.toRequest(bootcamp)).thenReturn(request);

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(bootcampHistoryWebClientAdapter.createBootcampHistory(bootcamp, "abc123"))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/v1/report/bootcamp-history/create", recordedRequest.getPath());
        assertEquals("Bearer abc123", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenCreateBootcampHistoryClientErrorOccurs() {
        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Java")
                .build();

        when(reportMapper.toRequest(any(Bootcamp.class)))
                .thenReturn(BootcampHistoryCreateRequest.builder()
                        .bootcampId(1L)
                        .name("Bootcamp Java")
                        .build());

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody("Error en servicio externo de reportes")
        );

        StepVerifier.create(bootcampHistoryWebClientAdapter.createBootcampHistory(bootcamp, "token"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalServiceException.class, error);
                    ExternalServiceException ex = (ExternalServiceException) error;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("Error en servicio externo de reportes", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldUpdateBootcampHistoryParticipantSuccessfully() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        ParticipantBootcampHistory participant = new ParticipantBootcampHistory(
                "Juan Perez",
                "juan@test.com"
        );

        StepVerifier.create(
                        bootcampHistoryWebClientAdapter.updateBootcampHistoryParticipant(participant, 1L, "token")
                )
                .verifyComplete();
    }

    @Test
    void shouldSendCorrectRequestWhenUpdatingBootcampHistoryParticipant() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        ParticipantBootcampHistory participant = new ParticipantBootcampHistory(
                "Juan Perez",
                "juan@test.com"
        );

        StepVerifier.create(
                        bootcampHistoryWebClientAdapter.updateBootcampHistoryParticipant(participant, 1L, "abc123")
                )
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("PATCH", recordedRequest.getMethod());
        assertEquals("/api/v1/report/bootcamp-history/update/1", recordedRequest.getPath());
        assertEquals("Bearer abc123", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenUpdatingBootcampHistoryParticipantServerErrorOccurs() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setBody("Error interno del servicio de reportes")
        );

        ParticipantBootcampHistory participant = new ParticipantBootcampHistory(
                "Juan Perez",
                "juan@test.com"
        );

        StepVerifier.create(
                        bootcampHistoryWebClientAdapter.updateBootcampHistoryParticipant(participant, 1L, "token")
                )
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalServiceException.class, error);
                    ExternalServiceException ex = (ExternalServiceException) error;
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
                    assertEquals("Error interno del servicio de reportes", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenReportServiceClientErrorBodyIsEmpty() {
        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Java")
                .build();

        when(reportMapper.toRequest(any(Bootcamp.class)))
                .thenReturn(BootcampHistoryCreateRequest.builder()
                        .bootcampId(1L)
                        .name("Bootcamp Java")
                        .build());

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
        );

        StepVerifier.create(bootcampHistoryWebClientAdapter.createBootcampHistory(bootcamp, "token"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalServiceException.class, error);
                    ExternalServiceException ex = (ExternalServiceException) error;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("Error en servicio externo de reportes", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenReportServiceServerErrorBodyIsEmpty() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(503)
        );

        ParticipantBootcampHistory participant = new ParticipantBootcampHistory(
                "Juan Perez",
                "juan@test.com"
        );

        StepVerifier.create(
                        bootcampHistoryWebClientAdapter.updateBootcampHistoryParticipant(participant, 1L, "token")
                )
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalServiceException.class, error);
                    ExternalServiceException ex = (ExternalServiceException) error;
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
                    assertEquals("Error interno del servicio de reportes", ex.getMessage());
                })
                .verify();
    }
}
