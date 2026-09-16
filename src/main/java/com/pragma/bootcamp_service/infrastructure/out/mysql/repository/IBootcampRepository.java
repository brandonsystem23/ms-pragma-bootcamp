package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM bootcamp
            WHERE name = :name
              AND status = true
        )
        """)
    Mono<Boolean> existsByName(String name);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc
                ON b.id = bc.bootcamp_id
               AND bc.status = true
            WHERE b.status = true
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            ORDER BY b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByNameAsc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc
                ON b.id = bc.bootcamp_id
               AND bc.status = true
            WHERE b.status = true
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            ORDER BY b.name DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByNameDesc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, b.status, COUNT(bc.capability_id) AS number_capabilities
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc
                ON b.id = bc.bootcamp_id
               AND bc.status = true
            WHERE b.status = true
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            ORDER BY COUNT(bc.capability_id) ASC, b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByCapabilityCountAsc(int size, long offset);

    @Query("""
            SELECT b.id, b.name, b.description, b.launch_date, b.duration_day, b.status, COUNT(bc.capability_id) AS number_capabilities
            FROM bootcamp b
            LEFT JOIN bootcamp_capability bc
                ON b.id = bc.bootcamp_id
               AND bc.status = true
            WHERE b.status = true
            GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_day, b.status
            ORDER BY COUNT(bc.capability_id) DESC, b.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllOrderByCapabilityCountDesc(int size, long offset);

    @Query("""
        SELECT COUNT(*)
        FROM bootcamp
        WHERE status = true
        """)
    Mono<Long> countAllBootcamps();

    @Query("""
    SELECT COUNT(*)
    FROM bootcamp_capability bc1
    INNER JOIN bootcamp_capability bc2
        ON bc2.capability_id = bc1.capability_id
    INNER JOIN bootcamp b2
        ON b2.id = bc2.bootcamp_id
    WHERE bc1.bootcamp_id = :bootcampId
      AND bc1.status = TRUE
      AND bc2.status = TRUE
      AND b2.status = TRUE
      AND bc2.bootcamp_id <> :bootcampId
    """)
    Mono<Long> countCapabilityUsageByOtherBootcamps(Long bootcampId);


    @Query("""
    SELECT COUNT(*)
    FROM bootcamp_capability bc1
    INNER JOIN capability_technology ct1
        ON ct1.capability_id = bc1.capability_id
    INNER JOIN capability_technology ct2
        ON ct2.technology_id = ct1.technology_id
    INNER JOIN bootcamp_capability bc2
        ON bc2.capability_id = ct2.capability_id
    INNER JOIN bootcamp b2
        ON b2.id = bc2.bootcamp_id
    WHERE bc1.bootcamp_id = :bootcampId
      AND bc1.status = TRUE
      AND ct1.status = TRUE
      AND ct2.status = TRUE
      AND bc2.status = TRUE
      AND b2.status = TRUE
      AND bc2.bootcamp_id <> :bootcampId
    """)
    Mono<Long> countTechnologyUsageByOtherBootcamps(Long bootcampId);



    @Modifying
    @Query("""
        UPDATE bootcamp
        SET status = :status
        WHERE id = :bootcampId
        """)
    Mono<Integer> updateStatusById(Long bootcampId, Boolean status);
}
