package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class BootcampRetrieveUseCase implements IBootcampRetrieveServicePort {

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final DomainBootcampValidator domainBootcampValidator;
    private final CapabilityDetailService capabilityDetailService;

    @Override
    public Mono<PagedResult<Bootcamp>> getBootcamps(
            BootcampPageCommand command,
            String token
    ) {
        return Mono.defer(() -> {
            domainBootcampValidator.validatePagination(command);

            return iBootcampPersistencePort.findAll(
                    command.page(),
                    command.size(),
                    command.sortBy(),
                    command.direction()
            );
        }).flatMap(pagedResult ->
                Flux.fromIterable(pagedResult.content())
                        .concatMap(bootcamp ->
                                enrichBootcamp(bootcamp, token)
                        )
                        .collectList()
                        .map(capabilities ->
                                PagedResult.<Bootcamp>builder()
                                        .content(capabilities)
                                        .page(pagedResult.page())
                                        .size(pagedResult.size())
                                        .totalElements(pagedResult.totalElements())
                                        .totalPages(pagedResult.totalPages())
                                        .first(pagedResult.first())
                                        .last(pagedResult.last())
                                        .build()
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