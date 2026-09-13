package com.pragma.bootcamp_service.domain.validation.capability;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
public class BootcampValidator {

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final ICapabilityWebClientPort iCapabilityWebClientPort;

    public Mono<Void> validateBootcamp(
            String name,
            List<Long> capabilityIds,
            String token
    ) {
        return validateCapabilityUniqueness(name)
                .then(Mono.defer(() -> validateTechnologies(capabilityIds, token)));
    }

    private Mono<Void> validateCapabilityUniqueness(String name) {
        return iBootcampPersistencePort.existsByName(name)
                .flatMap(bootcampAlreadyExists ->
                        Boolean.TRUE.equals(bootcampAlreadyExists)
                                ? Mono.error(new DomainException(
                                DomainErrorCode.DUPLICATE_NAME,
                                DomainErrorMessages.DUPLICATE_NAME
                        ))
                                : Mono.empty()
                );
    }

    private Mono<Void> validateTechnologies(
            List<Long> capabilityIds,
            String token
    ) {
        return iCapabilityWebClientPort.existsByIds(capabilityIds, token)
                .flatMap(existingCapabilityIds ->
                        existingCapabilityIds.size() == capabilityIds.size()
                                && new HashSet<>(existingCapabilityIds).containsAll(capabilityIds)
                                ? Mono.empty()
                                : Mono.error(new DomainException(
                                DomainErrorCode.CAPABILITY_NOT_FOUNT,
                                DomainErrorMessages.CAPABILITY_NOT_FOUND
                        ))
                );
    }

}
