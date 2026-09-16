package com.pragma.bootcamp_service.domain.api;

import reactor.core.publisher.Mono;

public interface IBootcampDeleteServicePort {

    Mono<Void> deleteById(Long bootcampId, String token);
}
