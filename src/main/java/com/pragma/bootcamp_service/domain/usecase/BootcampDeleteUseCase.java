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

import java.util.List;

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
                                DomainErrorMessages.DELETE_ID_REQUIRED
                        ));
                    }
                    return iBootcampPersistencePort.areResourcesUsedByOtherBootcamps(bootcampId)
                            .flatMap(usedByOthers -> {
                                if (Boolean.TRUE.equals(usedByOthers)) {
                                    return Mono.error(new DomainException(
                                            DomainErrorCode.VALIDATION_ERROR,
                                            DomainErrorMessages.RESOURCES_USED_BY_OTHER_BOOTCAMPS
                                    ));
                                }

                                return iBootcampPersistencePort.findCapabilityIdsByBootcampId(bootcampId)
                                        .flatMap(capabilityIds ->
                                                executeLocalSoftDelete(bootcampId)
                                                        .then(Mono.defer(() ->
                                                                callRemoteDeleteCapabilities(bootcampId, capabilityIds,
                                                                        token)))
                                        );
                            });
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof DomainException) {
                        return throwable;
                    }

                    return new DomainException(
                            DomainErrorCode.INTERNAL_ERROR,
                            DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR
                    );
                });
    }

    private Mono<Void> executeLocalSoftDelete(Long bootcampId) {
        return iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, false)
                .then(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, false))
                .as(transactionalOperator::transactional);
    }

    private Mono<Void> callRemoteDeleteCapabilities(Long bootcampId, List<Long> capabilityIds, String token) {
        if (capabilityIds.isEmpty()) {
            return Mono.empty();
        }

        return iCapabilityWebClientPort.deleteByIds(capabilityIds, token)
                .onErrorResume(throwable ->
                        rollbackStatuses(bootcampId)
                                .then(Mono.error(new DomainException(
                                        DomainErrorCode.INTERNAL_ERROR,
                                        DomainErrorMessages.BOOTCAMP_DELETE_ROLLBACK_ERROR
                                )))
                );
    }

    private Mono<Void> rollbackStatuses(Long bootcampId) {
        return iBootcampPersistencePort.updateBootcampCapabilitiesStatusByBootcampId(bootcampId, true)
                .then(iBootcampPersistencePort.updateBootcampStatusById(bootcampId, true))
                .as(transactionalOperator::transactional);
    }
}
