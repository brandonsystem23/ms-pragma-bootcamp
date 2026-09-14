package com.pragma.bootcamp_service.infrastructure.out.webclient.adapter;

import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.CapabilityDetailResponse;
import com.pragma.bootcamp_service.infrastructure.out.webclient.mapper.CapabilityMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CapabilityWebClientAdapterTest {

    private MockWebServer mockWebServer;
    private CapabilityWebClientAdapter capabilityWebClientAdapter;
    private CapabilityMapper capabilityMapper;

    @BeforeEach
    void setUp() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient capabilityWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        capabilityMapper = mock(CapabilityMapper.class);

        capabilityWebClientAdapter = new CapabilityWebClientAdapter(
                capabilityWebClient,
                "/api/v1/capability/exists-by-ids",
                "/api/v1/capability/by-ids",
                capabilityMapper
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFindExistingCapabilityIdsSuccessfully() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                [1, 2, 3]
                                """)
        );

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .assertNext(existingIds ->
                        assertEquals(
                                List.of(1L, 2L, 3L),
                                existingIds
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenNoCapabilitiesExist() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                []
                                """)
        );

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(999L, 1000L),
                                "token"
                        )
                )
                .assertNext(existingIds ->
                        assertTrue(existingIds.isEmpty())
                )
                .verifyComplete();
    }

    @Test
    void shouldSendCorrectRequest() throws InterruptedException {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("[1, 2, 3]")
        );

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "abc123"
                        )
                )
                .expectNext(List.of(1L, 2L, 3L))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("POST", request.getMethod());
        assertEquals(
                "/api/v1/capability/exists-by-ids",
                request.getPath()
        );
        assertEquals(
                "Bearer abc123",
                request.getHeader("Authorization")
        );
        assertEquals(
                "application/json",
                request.getHeader("Content-Type")
        );
        assertEquals(
                "[1,2,3]",
                request.getBody().readUtf8()
        );
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenClientErrorOccurs() {

        enqueueErrorResponse(
                400,
                "Error consultando las capacidades"
        );

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            exception.getStatus()
                    );

                    assertEquals(
                            "Error consultando las capacidades",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenClientErrorBodyIsEmpty() {

        enqueueErrorResponse(401, null);

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    assertEquals(
                            HttpStatus.UNAUTHORIZED,
                            exception.getStatus()
                    );

                    assertEquals(
                            "Error al consultar las capacidades",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenServerErrorOccurs() {

        enqueueErrorResponse(
                500,
                "Error interno del servicio de capacidades"
        );

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            exception.getStatus()
                    );

                    assertEquals(
                            "Error interno del servicio de capacidades",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenServerErrorBodyIsEmpty() {

        enqueueErrorResponse(503, null);

        StepVerifier.create(
                        capabilityWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    assertEquals(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            exception.getStatus()
                    );

                    assertEquals(
                            "Error interno del servicio de capacidades",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    private void enqueueErrorResponse(
            int statusCode,
            String body
    ) {

        // retry(2) = 1 intento inicial + 2 reintentos
        for (int i = 0; i < 3; i++) {

            MockResponse response = new MockResponse()
                    .setResponseCode(statusCode);

            if (body != null) {
                response.addHeader(
                        "Content-Type",
                        "text/plain"
                ).setBody(body);
            }

            mockWebServer.enqueue(response);
        }
    }

    @Test
    void shouldFindCapabilitiesByIdsSuccessfully() {

        Capability capability1 = Capability.builder()
                .id(1L)
                .name("Programación")
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .name("Bases de datos")
                .build();

        CapabilityDetailResponse response1 =
                CapabilityDetailResponse.builder()
                        .id(1L)
                        .name("Programación")
                        .build();

        CapabilityDetailResponse response2 =
                CapabilityDetailResponse.builder()
                        .id(2L)
                        .name("Bases de datos")
                        .build();

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                            [
                                {
                                    "id": 1,
                                    "name": "Programación"
                                },
                                {
                                    "id": 2,
                                    "name": "Bases de datos"
                                }
                            ]
                            """)
        );

        when(capabilityMapper.toModel(response1))
                .thenReturn(capability1);

        when(capabilityMapper.toModel(response2))
                .thenReturn(capability2);

        StepVerifier.create(
                        capabilityWebClientAdapter.findByIds(
                                List.of(1L, 2L),
                                "token"
                        )
                )
                .assertNext(capabilities -> {

                    assertEquals(2, capabilities.size());

                    assertEquals(
                            1L,
                            capabilities.get(0).getId()
                    );

                    assertEquals(
                            "Programación",
                            capabilities.get(0).getName()
                    );

                    assertEquals(
                            2L,
                            capabilities.get(1).getId()
                    );

                    assertEquals(
                            "Bases de datos",
                            capabilities.get(1).getName()
                    );
                })
                .verifyComplete();
    }
}

