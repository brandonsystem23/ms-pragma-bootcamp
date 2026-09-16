package com.pragma.bootcamp_service.domain.validation;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampValidatorTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private ICapabilityWebClientPort iCapabilityWebClientPort;

    @InjectMocks
    private BootcampValidator bootcampValidator;

    @Test
    void shouldCompleteWhenBootcampIsValid() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iCapabilityWebClientPort.existsByIds(capabilityIds, token))
                .thenReturn(Mono.just(capabilityIds));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .verifyComplete();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verify(iCapabilityWebClientPort)
                .existsByIds(capabilityIds, token);
    }

    @Test
    void shouldReturnErrorWhenBootcampNameAlreadyExists() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode()
                                        == DomainErrorCode.DUPLICATE_NAME &&
                                error.getMessage()
                                        .equals(DomainErrorMessages.DUPLICATE_NAME)
                )
                .verify();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verifyNoInteractions(iCapabilityWebClientPort);
    }

    @Test
    void shouldReturnErrorWhenCapabilityIdsDoNotExist() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        List<Long> existingCapabilityIds = List.of(1L, 2L);
        String token = "Bearer token";

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iCapabilityWebClientPort.existsByIds(
                capabilityIds,
                token
        )).thenReturn(Mono.just(existingCapabilityIds));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode()
                                        == DomainErrorCode.CAPABILITY_NOT_FOUNT &&
                                error.getMessage()
                                        .equals(DomainErrorMessages.CAPABILITY_NOT_FOUND)
                )
                .verify();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verify(iCapabilityWebClientPort)
                .existsByIds(capabilityIds, token);
    }

    @Test
    void shouldReturnErrorWhenCapabilityIdsAreDifferent() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        List<Long> existingCapabilityIds = List.of(1L, 2L, 4L);
        String token = "Bearer token";

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iCapabilityWebClientPort.existsByIds(
                capabilityIds,
                token
        )).thenReturn(Mono.just(existingCapabilityIds));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode()
                                        == DomainErrorCode.CAPABILITY_NOT_FOUNT &&
                                error.getMessage()
                                        .equals(DomainErrorMessages.CAPABILITY_NOT_FOUND)
                )
                .verify();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verify(iCapabilityWebClientPort)
                .existsByIds(capabilityIds, token);
    }

    @Test
    void shouldPropagateErrorWhenCheckingBootcampNameFails() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        RuntimeException exception =
                new RuntimeException("error consultando nombre");

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("error consultando nombre")
                )
                .verify();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verifyNoInteractions(iCapabilityWebClientPort);
    }

    @Test
    void shouldPropagateErrorWhenCheckingCapabilityIdsFails() {

        String name = "Desarrollo Backend";
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        RuntimeException exception =
                new RuntimeException("error consultando capacidades");

        when(iBootcampPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iCapabilityWebClientPort.existsByIds(
                capabilityIds,
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampValidator.validateBootcamp(
                                name,
                                capabilityIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("error consultando capacidades")
                )
                .verify();

        verify(iBootcampPersistencePort)
                .existsByName(name);

        verify(iCapabilityWebClientPort)
                .existsByIds(capabilityIds, token);
    }
}

