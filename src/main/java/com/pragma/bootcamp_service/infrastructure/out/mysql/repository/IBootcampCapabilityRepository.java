package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface IBootcampCapabilityRepository extends ReactiveCrudRepository<BootcampCapabilityEntity, Long> {

    Flux<BootcampCapabilityEntity> findAllByBootcampId(Long bootcampId);

}
