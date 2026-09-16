package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RequiredArgsConstructor
public class BootcampDeleteUseCase implements IBootcampDeleteServicePort {

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final ICapabilityWebClientPort iCapabilityWebClientPort;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> deleteById(Long bootcampId, String token) {

        return Mono.defer(() -> {

            if (bootcampId == null) {
                return Mono.error(new DomainException(
                        DomainErrorCode.VALIDATION_ERROR,
                        DomainErrorMessages.BOOTCAMP_ID_REQUIRED
                ));
            }

            return iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId)
                    .flatMap(capabilityIds ->
                            iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(capabilityIds, bootcampId)
                                    .flatMap(usedByOthers -> {
                                        if (Boolean.TRUE.equals(usedByOthers)) {
                                            return Mono.error(new DomainException(
                                                    DomainErrorCode.VALIDATION_ERROR,
                                                    DomainErrorMessages.RESOURCES_USED_BY_OTHER_BOOTCAMPS
                                            ));
                                        }

                                        return updateBootcampStatus(bootcampId, false)
                                                .then(Mono.defer(() ->
                                                        callRemoteDeleteCapabilities(bootcampId, capabilityIds, token)
                                                ));
                                    })
                    );

        }).onErrorMap(error -> {
            if (error instanceof DomainException) {
                return error;
            }

            return new DomainException(
                    DomainErrorCode.INTERNAL_ERROR,
                    DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR
            );
        });
    }

    private Mono<Void> callRemoteDeleteCapabilities(Long bootcampId, List<Long> capabilityIds, String token) {
        return iCapabilityWebClientPort.deleteByIds(capabilityIds, token)
                .onErrorResume(throwable ->
                        updateBootcampStatus(bootcampId, true)
                                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                                        .doBeforeRetry(retrySignal ->
                                                log.warn("Falló el rollback para el bootcamp {}. Reintento #{} debido a: {}",
                                                        bootcampId,
                                                        retrySignal.totalRetries(),
                                                        retrySignal.failure().getMessage()))
                                )
                                .onErrorResume(rollbackError -> {
                                    log.error("El rollback falló definitivamente tras agotar los reintentos para el bootcamp {}", bootcampId, rollbackError);
                                    return Mono.error(new DomainException(
                                            DomainErrorCode.INTERNAL_ERROR,
                                            DomainErrorMessages.ROLLBACK_ERROR
                                    ));
                                })
                                .then(Mono.error(new DomainException(
                                        DomainErrorCode.INTERNAL_ERROR,
                                        DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR
                                )))
                );
    }

    private Mono<Void> updateBootcampStatus(Long bootcampId, boolean status) {
        return iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, status)
                .then(Mono.defer(() -> iBootcampPersistencePort.updateBootcampStatusById(bootcampId, status)))
                .as(transactionalOperator::transactional);
    }
}
