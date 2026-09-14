package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {

    Mono<Boolean> existsByName(String name);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc ON b.id = bc.bootcamp_id
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day
            ORDER BY b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByNameAsc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc ON b.id = bc.bootcamp_id
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day
            ORDER BY b.name DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByNameDesc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, COUNT(bc.capability_id) AS number_capabilities
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc ON b.id = bc.bootcamp_id
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day
            ORDER BY COUNT(bc.capability_id) ASC, b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByCapabilityCountAsc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, COUNT(bc.capability_id) AS number_capabilities
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc ON b.id = bc.bootcamp_id
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day
            ORDER BY COUNT(bc.capability_id) DESC, b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByCapabilityCountDesc(int size, long offset);

    @Query("SELECT COUNT(*) FROM bootcamp")
    Mono<Long> countAllBootcamps();
}
