package com.pragma.bootcamp_service.infrastructure.out.mysql.repository;

import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface IBootcampCapabilityRepository extends ReactiveCrudRepository<BootcampCapabilityEntity, Long> {

}
