package com.pragma.bootcamp_service.domain.service;

import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityDetailServiceTest {

    @Mock
    private ICapabilityWebClientPort iCapabilityWebClientPort;

    @InjectMocks
    private CapabilityDetailService capabilityDetailService;

    @Test
    void shouldEnrichCapabilitiesSuccessfully() {

        Capability capability1 = Capability.builder()
                .id(1L)
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .build();

        List<Capability> capabilities = List.of(
                capability1,
                capability2
        );

        String token = "Bearer token";

        when(iCapabilityWebClientPort.findByIds(
                List.of(1L, 2L),
                token
        )).thenReturn(Mono.just(capabilities));

        StepVerifier.create(
                        capabilityDetailService.enrich(capabilities, token)
                )
                .assertNext(result -> {
                    assertEquals(2, result.size());
                    assertEquals(1L, result.get(0).getId());
                    assertEquals(2L, result.get(1).getId());
                })
                .verifyComplete();

        verify(iCapabilityWebClientPort).findByIds(
                List.of(1L, 2L),
                token
        );

        verifyNoMoreInteractions(iCapabilityWebClientPort);
    }

    @Test
    void shouldRemoveDuplicatedCapabilityIdsBeforeCallingWebClient() {

        Capability capability1 = Capability.builder()
                .id(1L)
                .build();

        Capability capability1Duplicated = Capability.builder()
                .id(1L)
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .build();

        List<Capability> capabilities = List.of(
                capability1,
                capability1Duplicated,
                capability2
        );

        String token = "Bearer token";

        List<Capability> enrichedCapabilities = List.of(
                capability1,
                capability2
        );

        when(iCapabilityWebClientPort.findByIds(
                List.of(1L, 2L),
                token
        )).thenReturn(Mono.just(enrichedCapabilities));

        StepVerifier.create(
                        capabilityDetailService.enrich(capabilities, token)
                )
                .assertNext(result -> {
                    assertEquals(2, result.size());
                    assertEquals(1L, result.get(0).getId());
                    assertEquals(2L, result.get(1).getId());
                })
                .verifyComplete();

        verify(iCapabilityWebClientPort).findByIds(
                List.of(1L, 2L),
                token
        );

        verifyNoMoreInteractions(iCapabilityWebClientPort);
    }

    @Test
    void shouldReturnEmptyListWhenNoCapabilitiesAreProvided() {

        List<Capability> capabilities = List.of();
        String token = "Bearer token";

        when(iCapabilityWebClientPort.findByIds(
                List.of(),
                token
        )).thenReturn(Mono.just(List.of()));

        StepVerifier.create(
                        capabilityDetailService.enrich(capabilities, token)
                )
                .assertNext(result -> assertEquals(0, result.size()))
                .verifyComplete();

        verify(iCapabilityWebClientPort).findByIds(
                List.of(),
                token
        );

        verifyNoMoreInteractions(iCapabilityWebClientPort);
    }

    @Test
    void shouldPropagateErrorWhenFindingCapabilities() {

        Capability capability = Capability.builder()
                .id(1L)
                .build();

        List<Capability> capabilities = List.of(capability);
        String token = "Bearer token";

        RuntimeException exception =
                new RuntimeException("Error consultando capacidades");

        when(iCapabilityWebClientPort.findByIds(
                List.of(1L),
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        capabilityDetailService.enrich(capabilities, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "Error consultando capacidades"
                                )
                )
                .verify();

        verify(iCapabilityWebClientPort).findByIds(
                List.of(1L),
                token
        );

        verifyNoMoreInteractions(iCapabilityWebClientPort);
    }
}