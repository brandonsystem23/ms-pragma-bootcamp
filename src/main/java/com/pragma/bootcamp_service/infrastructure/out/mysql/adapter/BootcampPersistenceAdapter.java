package com.pragma.bootcamp_service.infrastructure.out.mysql.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.mapper.BootcampEntityMapper;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampRepository;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampCapabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampPersistenceAdapter implements IBootcampPersistencePort {

    private final IBootcampRepository iBootcampRepository;
    private final IBootcampCapabilityRepository iBootcampCapabilityRepository;
    private final BootcampEntityMapper bootcampEntityMapper;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        BootcampEntity bootcampEntity = bootcampEntityMapper.toEntity(bootcamp);
        return iBootcampRepository.save(bootcampEntity)
                .flatMap(savedBootcampEntity -> saveItems(savedBootcampEntity.getId(), bootcamp.getCapabilities())
                        .map(savedItems -> buildOrder(savedBootcampEntity, savedItems)));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return iBootcampRepository.existsByName(name);
    }


    private Mono<List<Capability>> saveItems(Long bootcampId, List<Capability> items) {

        List<BootcampCapabilityEntity> entities = items.stream()
                .map(item -> BootcampCapabilityEntity.builder()
                        .bootcampId(bootcampId)
                        .capabilityId(item.getId())
                        .build()
                )
                .toList();

        return iBootcampCapabilityRepository.saveAll(entities)
                .map(bootcampEntityMapper::toCapability)
                .collectList();
    }

    private Bootcamp buildOrder(BootcampEntity bootcampEntity, List<Capability> items) {
        Bootcamp bootcamp = bootcampEntityMapper.toDomain(bootcampEntity);
        bootcamp.setCapabilities(items);
        return bootcamp;
    }
}