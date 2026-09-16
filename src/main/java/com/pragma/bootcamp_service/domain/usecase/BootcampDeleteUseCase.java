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
