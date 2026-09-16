package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampParticipantEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampParticipantRepository extends ReactiveCrudRepository<BootcampParticipantEntity, Long> {

    @Query("""
            SELECT count(id)
            FROM bootcamp_participant
            WHERE bootcamp_id = :bootcampId
              AND participant_id = :participantId
              AND status = true
        """)
    Mono<Long> existsByBootcampIdAndParticipantId(Long bootcampId, Long participantId);

    @Query("""
        SELECT COUNT(*)
        FROM bootcamp_participant bp
        INNER JOIN bootcamp b
            ON b.id = bp.bootcamp_id
        WHERE bp.participant_id = :participantId
          AND bp.status = true
          AND b.status = true
        """)
    Mono<Long> countActiveBootcampsByParticipantId(Long participantId);

    @Query("""
        SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
        FROM bootcamp_participant bp
        INNER JOIN bootcamp b
            ON b.id = bp.bootcamp_id
        WHERE bp.participant_id = :participantId
          AND bp.status = true
          AND b.status = true
        """)
    Flux<BootcampEntity> findActiveBootcampsByParticipantId(Long participantId);
}
