package com.pragma.bootcamp_service.infrastructure.out.mysql.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.mapper.BootcampEntityMapper;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampCapabilityRepository;
import com.pragma.bootcamp_service.infrastructure.out.mysql.repository.IBootcampRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampPersistenceAdapterTest {

    @Mock
    private IBootcampRepository iBootcampRepository;

    @Mock
    private IBootcampCapabilityRepository iBootcampCapabilityRepository;

    @Mock
    private BootcampEntityMapper bootcampEntityMapper;

    @InjectMocks
    private BootcampPersistenceAdapter bootcampPersistenceAdapter;

    @Test
    void shouldSaveBootcampSuccessfully() {

        Capability capability1 = Capability.builder().id(1L).build();
        Capability capability2 = Capability.builder().id(2L).build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(null)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .capabilities(List.of(capability1, capability2))
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .build();

        BootcampEntity savedBootcampEntity = BootcampEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity1 = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(1L)
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity2 = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(2L)
                .status(true)
                .build();

        Bootcamp mappedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .capabilities(List.of())
                .build();

        when(bootcampEntityMapper.toEntity(bootcamp)).thenReturn(bootcampEntity);
        when(iBootcampRepository.save(bootcampEntity)).thenReturn(Mono.just(savedBootcampEntity));
        when(iBootcampCapabilityRepository.saveAll(anyList())).thenReturn(Flux.just(capabilityEntity1, capabilityEntity2));
        when(bootcampEntityMapper.toCapability(capabilityEntity1)).thenReturn(capability1);
        when(bootcampEntityMapper.toCapability(capabilityEntity2)).thenReturn(capability2);
        when(bootcampEntityMapper.toDomain(savedBootcampEntity)).thenReturn(mappedBootcamp);

        StepVerifier.create(bootcampPersistenceAdapter.save(bootcamp))
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertEquals("Desarrollo Backend", result.getName());
                    assertEquals("Bootcamp de desarrollo backend", result.getDescription());
                    assertEquals(List.of(capability1, capability2), result.getCapabilities());
                    assertTrue(result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldCheckIfBootcampExistsByName() {
        String name = "Desarrollo Backend";

        when(iBootcampRepository.existsByName(name)).thenReturn(Mono.just(true));

        StepVerifier.create(bootcampPersistenceAdapter.existsByName(name))
                .expectNext(true)
                .verifyComplete();

        verify(iBootcampRepository).existsByName(name);
    }

    @Test
    void shouldFindAllBootcampsOrderByNameAsc() {
        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(10L)
                .status(true)
                .build();

        when(iBootcampRepository.findAllOrderByNameAsc(10, 0L)).thenReturn(Flux.just(entity));
        when(iBootcampCapabilityRepository.findAllByBootcampId(1L)).thenReturn(Flux.just(capabilityEntity));
        when(bootcampEntityMapper.toDomain(entity)).thenReturn(bootcamp);
        when(iBootcampRepository.countAllBootcamps()).thenReturn(Mono.just(1L));

        StepVerifier.create(bootcampPersistenceAdapter.findAll(0, 10, "name", "asc"))
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.content().getFirst().getId());
                    assertEquals("Bootcamp Backend", result.content().getFirst().getName());
                    assertEquals(
                            List.of(Capability.builder().id(10L).build()),
                            result.content().getFirst().getCapabilities()
                    );
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                    assertTrue(result.first());
                    assertTrue(result.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindCapabilityIdsByBootcampIdSuccessfully() {
        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.just(
                        BootcampCapabilityEntity.builder().bootcampId(1L).capabilityId(10L).status(true).build(),
                        BootcampCapabilityEntity.builder().bootcampId(1L).capabilityId(20L).status(true).build()
                ));

        StepVerifier.create(bootcampPersistenceAdapter.findCapabilityIdsByBootcampId(1L))
                .assertNext(ids -> assertEquals(List.of(10L, 20L), ids))
                .verifyComplete();
    }



    @Test
    void shouldReturnIfResourcesAreUsedByOtherBootcamps() {

        when(iBootcampRepository.countCapabilityUsageByOtherBootcamps(1L))
                .thenReturn(Mono.just(1L));

        when(iBootcampRepository.countTechnologyUsageByOtherBootcamps(1L))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.areResourcesUsedByOtherBootcamps(1L)
                )
                .expectNext(true)
                .verifyComplete();


    }

    @Test
    void shouldUpdateBootcampCapabilitiesStatusByBootcampIdSuccessfully() {
        when(iBootcampCapabilityRepository.updateStatusByBootcampId(1L, false))
                .thenReturn(Mono.just(2));

        StepVerifier.create(bootcampPersistenceAdapter.updateBootcampCapabilitiesStatusByBootcampId(1L, false))
                .verifyComplete();
    }

    @Test
    void shouldUpdateBootcampStatusByIdSuccessfully() {
        when(iBootcampRepository.updateStatusById(1L, false))
                .thenReturn(Mono.just(1));

        StepVerifier.create(bootcampPersistenceAdapter.updateBootcampStatusById(1L, false))
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenSavingBootcamp() {
        Bootcamp bootcamp = Bootcamp.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .capabilities(List.of(Capability.builder().id(1L).build()))
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .build();

        RuntimeException exception = new RuntimeException("Error guardando bootcamp");

        when(bootcampEntityMapper.toEntity(bootcamp)).thenReturn(bootcampEntity);
        when(iBootcampRepository.save(bootcampEntity)).thenReturn(Mono.error(exception));

        StepVerifier.create(bootcampPersistenceAdapter.save(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error guardando bootcamp"))
                .verify();

        verify(bootcampEntityMapper).toEntity(bootcamp);
        verify(iBootcampRepository).save(bootcampEntity);
        verifyNoInteractions(iBootcampCapabilityRepository);
    }

    @Test
    void shouldPropagateErrorWhenSavingBootcampCapabilities() {
        Capability capability = Capability.builder().id(1L).build();

        Bootcamp bootcamp = Bootcamp.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .capabilities(List.of(capability))
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .build();

        BootcampEntity savedBootcampEntity = BootcampEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .status(true)
                .build();

        RuntimeException exception = new RuntimeException("Error guardando relaciones del bootcamp");

        when(bootcampEntityMapper.toEntity(bootcamp)).thenReturn(bootcampEntity);
        when(iBootcampRepository.save(bootcampEntity)).thenReturn(Mono.just(savedBootcampEntity));
        when(iBootcampCapabilityRepository.saveAll(anyList())).thenReturn(Flux.error(exception));

        StepVerifier.create(bootcampPersistenceAdapter.save(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error guardando relaciones del bootcamp"))
                .verify();
    }

    @Test
    void shouldUpdateBootcampCapabilitiesStatusByBootcampIdPropagateError() {
        when(iBootcampCapabilityRepository.updateStatusByBootcampId(1L, true))
                .thenReturn(Mono.error(new RuntimeException("error actualizando relaciones")));

        StepVerifier.create(bootcampPersistenceAdapter.updateBootcampCapabilitiesStatusByBootcampId(1L, true))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error actualizando relaciones"))
                .verify();
    }

    @Test
    void shouldUpdateBootcampStatusByIdPropagateError() {
        when(iBootcampRepository.updateStatusById(1L, true))
                .thenReturn(Mono.error(new RuntimeException("error actualizando bootcamp")));

        StepVerifier.create(bootcampPersistenceAdapter.updateBootcampStatusById(1L, true))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error actualizando bootcamp"))
                .verify();
    }

    @Test
    void shouldFindAllBootcampsOrderByNameDesc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(10L)
                .status(true)
                .build();

        when(iBootcampRepository.findAllOrderByNameDesc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.just(capabilityEntity));

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(0, 10, "name", "desc")
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.content().getFirst().getId());
                    assertEquals("Bootcamp Backend",
                            result.content().getFirst().getName());
                    assertEquals(
                            List.of(Capability.builder().id(10L).build()),
                            result.content().getFirst().getCapabilities()
                    );
                })
                .verifyComplete();

        verify(iBootcampRepository)
                .findAllOrderByNameDesc(10, 0L);
    }

    @Test
    void shouldFindAllBootcampsOrderByNumberCapabilitiesAsc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(10L)
                .status(true)
                .build();

        when(iBootcampRepository.findAllOrderByCapabilityCountAsc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.just(capabilityEntity));

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0, 10, "numbercapabilities", "asc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.content().getFirst().getId());
                    assertEquals("Bootcamp Backend",
                            result.content().getFirst().getName());
                    assertEquals(
                            List.of(Capability.builder().id(10L).build()),
                            result.content().getFirst().getCapabilities()
                    );
                })
                .verifyComplete();

        verify(iBootcampRepository)
                .findAllOrderByCapabilityCountAsc(10, 0L);
    }

    @Test
    void shouldFindAllBootcampsOrderByNumberCapabilitiesDesc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .status(true)
                .build();

        BootcampCapabilityEntity capabilityEntity = BootcampCapabilityEntity.builder()
                .bootcampId(1L)
                .capabilityId(10L)
                .status(true)
                .build();

        when(iBootcampRepository.findAllOrderByCapabilityCountDesc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.just(capabilityEntity));

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0, 10, "numbercapabilities", "desc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.content().getFirst().getId());
                    assertEquals("Bootcamp Backend",
                            result.content().getFirst().getName());
                    assertEquals(
                            List.of(Capability.builder().id(10L).build()),
                            result.content().getFirst().getCapabilities()
                    );
                })
                .verifyComplete();

        verify(iBootcampRepository)
                .findAllOrderByCapabilityCountDesc(10, 0L);
    }
}
