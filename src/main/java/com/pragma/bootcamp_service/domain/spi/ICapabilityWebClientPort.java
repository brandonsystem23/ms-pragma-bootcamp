package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Capability;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityWebClientPort {

    Mono<List<Long>> existsByIds(List<Long> ids, String token);

    Mono<List<Capability>> findByIds(List<Long> ids, String token);

    Mono<Void> deleteByIds(List<Long> ids, String token);
}
