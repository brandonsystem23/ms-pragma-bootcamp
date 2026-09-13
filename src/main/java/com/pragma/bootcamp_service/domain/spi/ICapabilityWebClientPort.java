package com.pragma.bootcamp_service.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityWebClientPort {

    Mono<List<Long>> existsByIds(List<Long> ids, String token);

}
