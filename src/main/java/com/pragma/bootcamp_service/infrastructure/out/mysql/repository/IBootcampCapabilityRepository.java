package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampCapabilityRepository extends ReactiveCrudRepository<BootcampCapabilityEntity, Long> {

    @Query("""
        SELECT id, bootcamp_id, capability_id, status
        FROM bootcamp_capability
        WHERE bootcamp_id = :bootcampId
          AND status = true
        """)
    Flux<BootcampCapabilityEntity> findAllByBootcampId(Long bootcampId);

    @Modifying
    @Query("""
        UPDATE bootcamp_capability
        SET status = :status
        WHERE bootcamp_id = :bootcampId
        """)
    Mono<Integer> updateStatusByBootcampId(Long bootcampId, Boolean status);
}
