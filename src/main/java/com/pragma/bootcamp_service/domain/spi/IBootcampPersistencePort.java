package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBootcampPersistencePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Boolean> existsByName(String name);

    Mono<PagedResult<Bootcamp>> findAll(int page, int size, String sortBy, String direction);

    Mono<List<Long>> findCapabilityIdsByBootcampId(Long bootcampId);

    Mono<Boolean> areResourcesUsedByOtherBootcamps(List<Long> capabilityIds, Long bootcampId);

    Mono<Void> updateBootcampCapabilitiesStatusByBootcampId(Long bootcampId, Boolean status);

    Mono<Void> updateBootcampStatusById(Long bootcampId, Boolean status);
}
