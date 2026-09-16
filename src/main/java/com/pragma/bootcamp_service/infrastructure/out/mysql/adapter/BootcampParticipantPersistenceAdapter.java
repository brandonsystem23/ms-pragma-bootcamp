package com.pragma.bootcamp_service.infrastructure.out.mysql.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampParticipantEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.mapper.BootcampEntityMapper;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BootcampParticipantPersistenceAdapter implements IBootcampParticipantPersistencePort {

    private final IBootcampParticipantRepository iBootcampParticipantRepository;
    private final BootcampEntityMapper bootcampEntityMapper;

    @Override
    public Mono<Boolean> existsByBootcampIdAndParticipantId(Long bootcampId, Long participantId) {
        return iBootcampParticipantRepository.existsByBootcampIdAndParticipantId(bootcampId, participantId)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Long> countActiveBootcampsByParticipantId(Long participantId) {
        return iBootcampParticipantRepository.countActiveBootcampsByParticipantId(participantId);
    }

    @Override
    public Flux<Bootcamp> findActiveBootcampsByParticipantId(Long participantId) {
        return iBootcampParticipantRepository.findActiveBootcampsByParticipantId(participantId)
                .map(bootcampEntityMapper::toDomain);
    }

    @Override
    public Mono<Void> saveEnrollment(Long bootcampId, Long participantId) {
        BootcampParticipantEntity bootcampParticipantEntity = BootcampParticipantEntity.builder()
                .bootcampId(bootcampId)
                .participantId(participantId)
                .status(true)
                .build();

        return iBootcampParticipantRepository.save(bootcampParticipantEntity)
                .then();
    }
}
