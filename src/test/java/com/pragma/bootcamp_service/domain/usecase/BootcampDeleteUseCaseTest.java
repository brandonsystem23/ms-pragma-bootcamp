package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampDeleteUseCaseTest {

    @Mock
    private IBootcampPersistencePort iBootcampPersistencePort;

    @Mock
    private ICapabilityWebClientPort iCapabilityWebClientPort;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private BootcampDeleteUseCase bootcampDeleteUseCase;

    @Test
    void shouldSoftDeleteBootcampSuccessfully() {
        Long bootcampId = 1L;
        String token = "token";
        List<Long> capabilityIds = List.of(10L, 20L);

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(false));
        when(iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Mono.just(capabilityIds));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iCapabilityWebClientPort.deleteByIds(capabilityIds, token))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, token))
                .verifyComplete();
    }

    @Test
    void shouldSoftDeleteBootcampWithoutCallingWebClientWhenNoCapabilitiesExist() {
        Long bootcampId = 1L;
        String token = "token";

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(false));
        when(iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Mono.just(List.of()));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, false))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, token))
                .verifyComplete();
    }

    @Test
    void shouldReturnValidationErrorWhenBootcampIdIsNull() {
        StepVerifier.create(bootcampDeleteUseCase.deleteById(null, "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.DELETE_ID_REQUIRED))
                .verify();
    }

    @Test
    void shouldReturnValidationErrorWhenResourcesAreUsedByOtherBootcamps() {
        Long bootcampId = 1L;

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(true));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.RESOURCES_USED_BY_OTHER_BOOTCAMPS))
                .verify();
    }

    @Test
    void shouldReturnInternalErrorWhenSoftDeleteCapabilitiesFails() {
        Long bootcampId = 1L;

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(false));
        when(iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Mono.just(List.of(10L)));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false))
                .thenReturn(Mono.error(new RuntimeException("error actualizando bootcamp_capability")));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR))
                .verify();
    }

    @Test
    void shouldReturnInternalErrorWhenSoftDeleteBootcampFails() {
        Long bootcampId = 1L;

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(false));
        when(iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Mono.just(List.of(10L)));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, false))
                .thenReturn(Mono.error(new RuntimeException("error actualizando bootcamp")));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR))
                .verify();
    }

    @Test
    void shouldRollbackStatusesWhenCapabilityWebClientFails() {
        Long bootcampId = 1L;
        String token = "token";
        List<Long> capabilityIds = List.of(10L);

        when(iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId))
                .thenReturn(Mono.just(false));
        when(iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId))
                .thenReturn(Mono.just(capabilityIds));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, false))
                .thenReturn(Mono.empty());
        when(iCapabilityWebClientPort.deleteByIds(capabilityIds, token))
                .thenReturn(Mono.error(new RuntimeException("rollback remoto")));
        when(iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, true))
                .thenReturn(Mono.empty());
        when(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, true))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(bootcampDeleteUseCase.deleteById(bootcampId, token))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR))
                .verify();
    }
}
