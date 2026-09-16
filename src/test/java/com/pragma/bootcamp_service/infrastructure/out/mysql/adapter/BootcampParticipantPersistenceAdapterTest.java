package com.pragma.bootcamp_service.infrastructure.out.mysql.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampParticipantEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.mapper.BootcampEntityMapper;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampParticipantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampParticipantPersistenceAdapterTest {

    @Mock
    private IBootcampParticipantRepository iBootcampParticipantRepository;

    @Mock
    private BootcampEntityMapper bootcampEntityMapper;

    @InjectMocks
    private BootcampParticipantPersistenceAdapter bootcampParticipantPersistenceAdapter;

    @Test
    void shouldCheckIfEnrollmentExists() {
        when(iBootcampParticipantRepository.existsByBootcampIdAndParticipantId(1L, 100L))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(bootcampParticipantPersistenceAdapter.existsByBootcampIdAndParticipantId(1L, 100L))
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    void shouldCountActiveBootcampsByParticipantId() {
        when(iBootcampParticipantRepository.countActiveBootcampsByParticipantId(100L))
                .thenReturn(Mono.just(3L));

        StepVerifier.create(bootcampParticipantPersistenceAdapter.countActiveBootcampsByParticipantId(100L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void shouldFindActiveBootcampsByParticipantId() {
        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 10))
                .durationDay(10)
                .status(true)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Java")
                .description("Desc")
                .launchDate(LocalDate.of(2026, 10, 10))
                .durationDay(10)
                .status(true)
                .build();

        when(iBootcampParticipantRepository.findActiveBootcampsByParticipantId(100L))
                .thenReturn(Flux.just(entity));

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        StepVerifier.create(bootcampParticipantPersistenceAdapter.findActiveBootcampsByParticipantId(100L))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    void shouldSaveEnrollmentSuccessfully() {
        BootcampParticipantEntity entity = BootcampParticipantEntity.builder()
                .bootcampId(1L)
                .participantId(100L)
                .status(true)
                .build();

        when(iBootcampParticipantRepository.save(entity))
                .thenReturn(Mono.just(entity));

        StepVerifier.create(bootcampParticipantPersistenceAdapter.saveEnrollment(1L, 100L))
                .verifyComplete();

    }
}
