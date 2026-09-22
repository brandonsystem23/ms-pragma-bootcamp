package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.builder.BootcampBuilder;
import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IReportWebClientPort;
import com.pragma.bootcamp_service.domain.validation.bootcamp.DomainBootcampValidator;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class BootcampRegisterUseCase implements IBootcampRegisterServicePort {

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final IReportWebClientPort iReportWebClientPort;
    private final DomainBootcampValidator domainBootcampValidator;
    private final BootcampValidator bootcampValidator;
    private final CapabilityDetailService capabilityDetailService;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<Bootcamp> create(BootcampCommand bootcampCommand, String token) {
        return Mono.defer(() -> {
            domainBootcampValidator.validateBootcampCommand(bootcampCommand);

            return bootcampValidator.validateBootcamp(bootcampCommand.name(),
                            bootcampCommand.capabilityIds(), token)
                    .then(Mono.defer(() -> {
                        Bootcamp bootcamp = BootcampBuilder.buildBootcamp(bootcampCommand);
                        return iBootcampPersistencePort.save(bootcamp);
                    }));
        })
                .as(transactionalOperator::transactional)
                .onErrorMap(throwable -> {
                    if (throwable instanceof DomainException) {
                        return throwable;
                    }

                    return new DomainException(
                            DomainErrorCode.INTERNAL_ERROR,
                            DomainErrorMessages.BOOTCAMP_SAVE_ROLLBACK_ERROR
                    );
                })
                .flatMap(bootcampSave -> enrichBootcamp(bootcampSave, token)
                        .doOnError(error ->
                                        log.error("No se pudo enriquecer el bootcamp {}", bootcampSave.getId(), error)
                                )
                        .onErrorReturn(bootcampSave)
                )
                .flatMap(bootcampFull ->
                        iReportWebClientPort
                                .createBootcampHistory(bootcampFull, token)
                                .thenReturn(bootcampFull)
                                .onErrorMap(error ->
                                        new DomainException(
                                                DomainErrorCode.INTERNAL_ERROR,
                                                DomainErrorMessages.BOOTCAMP_HISTORY_SAVE_ERROR
                                        )
                                )
                );
    }

    private Mono<Bootcamp> enrichBootcamp(Bootcamp bootcamp, String token) {

        log.info(
                "Bootcamp {} - capabilities: {}",
                bootcamp.getId(),
                bootcamp.getCapabilities()
        );
        return capabilityDetailService.enrich(bootcamp.getCapabilities(), token)
                .map(capabilities -> {
                    bootcamp.setCapabilities(capabilities);
                    return bootcamp;
                });
    }
}
