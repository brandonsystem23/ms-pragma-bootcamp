package com.pragma.bootcamp_service.infrastructure.out.mysql.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.FilterValues;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.mapper.BootcampEntityMapper;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampCapabilityRepository;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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
        bootcampEntity.setStatus(true);

        return iBootcampRepository.save(bootcampEntity)
                .flatMap(savedBootcampEntity ->
                        saveItems(savedBootcampEntity.getId(), bootcamp.getCapabilities())
                                .map(savedItems -> buildBootcamp(savedBootcampEntity, savedItems))
                );
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return iBootcampRepository.existsByName(name);
    }

    @Override
    public Mono<PagedResult<Bootcamp>> findAll(int page, int size, String sortBy, String direction) {

        long offset = (long) page * size;

        Flux<BootcampEntity> bootcamps = switch (sortBy.toLowerCase()) {
            case FilterValues.NAME -> FilterValues.DESCENDING.equalsIgnoreCase(direction)
                    ? iBootcampRepository.findAllOrderByNameDesc(size, offset)
                    : iBootcampRepository.findAllOrderByNameAsc(size, offset);

            case FilterValues.NUMBER_CAPABILITIES -> FilterValues.DESCENDING.equalsIgnoreCase(direction)
                    ? iBootcampRepository.findAllOrderByCapabilityCountDesc(size, offset)
                    : iBootcampRepository.findAllOrderByCapabilityCountAsc(size, offset);

            default -> iBootcampRepository.findAllOrderByNameAsc(size, offset);
        };

        Mono<List<Bootcamp>> bootcampsWithCapabilities = bootcamps
                .concatMap(bootcampEntity ->
                        findCapabilityIdsByBootcampId(bootcampEntity.getId())
                                .map(capabilityIds -> {
                                    Bootcamp bootcamp = bootcampEntityMapper.toDomain(bootcampEntity);

                                    List<Capability> capabilities = capabilityIds.stream()
                                            .map(capabilityId -> Capability.builder()
                                                    .id(capabilityId)
                                                    .build())
                                            .toList();

                                    bootcamp.setCapabilities(capabilities);

                                    return bootcamp;
                                })
                )
                .collectList();

        return Mono.zip(
                bootcampsWithCapabilities,
                iBootcampRepository.countAllBootcamps()
        ).map(tuple -> {
            List<Bootcamp> content = tuple.getT1();
            long totalElements = tuple.getT2();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            return PagedResult.<Bootcamp>builder()
                    .content(content)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(page == 0)
                    .last(page >= totalPages - 1)
                    .build();
        });
    }

    @Override
    public Mono<List<Long>> findCapabilityIdsByBootcampId(Long bootcampId) {
        return iBootcampCapabilityRepository.findAllByBootcampId(bootcampId)
                .map(BootcampCapabilityEntity::getCapabilityId)
                .collectList();
    }

    @Override
    public Mono<Bootcamp> findActiveById(Long bootcampId) {
        return iBootcampRepository.findActiveById(bootcampId)
                .map(bootcampEntityMapper::toDomain);
    }


    @Override
    public Mono<Boolean> areResourcesUsedByOtherBootcamps(List<Long> capabilityIds, Long bootcampId) {
        return Mono.zip(
                iBootcampRepository.countCapabilityUsageByOtherBootcamps(capabilityIds, bootcampId),
                iBootcampRepository.countTechnologyUsageByOtherBootcamps(capabilityIds, bootcampId)
        ).map(tuple ->
                isPositive(tuple.getT1()) || isPositive(tuple.getT2())
        );
    }

    private boolean isPositive(Long num) {
        return num > 0;
    }

    @Override
    public Mono<Void> updateBootcampCapabilitiesStatusByBootcampId(Long bootcampId, Boolean status) {
        return iBootcampCapabilityRepository.updateStatusByBootcampId(bootcampId, status).then();
    }

    @Override
    public Mono<Void> updateBootcampStatusById(Long bootcampId, Boolean status) {
        return iBootcampRepository.updateStatusById(bootcampId, status).then();
    }

    private Mono<List<Capability>> saveItems(Long bootcampId, List<Capability> items) {

        List<BootcampCapabilityEntity> entities = items.stream()
                .map(item -> BootcampCapabilityEntity.builder()
                        .bootcampId(bootcampId)
                        .capabilityId(item.getId())
                        .status(true)
                        .build())
                .toList();

        return iBootcampCapabilityRepository.saveAll(entities)
                .map(bootcampEntityMapper::toCapability)
                .collectList();
    }

    private Bootcamp buildBootcamp(BootcampEntity bootcampEntity, List<Capability> items) {
        Bootcamp bootcamp = bootcampEntityMapper.toDomain(bootcampEntity);
        bootcamp.setCapabilities(items);
        return bootcamp;
    }
}
