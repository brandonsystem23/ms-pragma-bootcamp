package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import reactor.core.publisher.Mono;


public interface IBootcampPersistencePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Boolean> existsByName(String name);

    Mono<PagedResult<Bootcamp>> findAll(int page, int size, String sortBy, String direction);

}
