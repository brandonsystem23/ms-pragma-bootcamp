package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampParticipantPersistencePort {

    Mono<Boolean> existsByBootcampIdAndParticipantId(Long bootcampId, Long participantId);

    Mono<Long> countActiveBootcampsByParticipantId(Long participantId);

    Flux<Bootcamp> findActiveBootcampsByParticipantId(Long participantId);

    Mono<Void> saveEnrollment(Long bootcampId, Long participantId);
}
