package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import reactor.core.publisher.Mono;



public interface IBootcampPersistencePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Boolean> existsByName(String name);

}
