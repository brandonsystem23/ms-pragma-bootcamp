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
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private BootcampPersistenceAdapter bootcampPersistenceAdapter;

    @Test
    void shouldSaveBootcampSuccessfully() {

        Capability capability1 = Capability.builder()
                .id(1L)
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(null)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .capabilities(List.of(capability1, capability2))
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .build();

        BootcampEntity savedBootcampEntity = BootcampEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .build();

        BootcampCapabilityEntity capabilityEntity1 =
                BootcampCapabilityEntity.builder()
                        .bootcampId(1L)
                        .capabilityId(1L)
                        .build();

        BootcampCapabilityEntity capabilityEntity2 =
                BootcampCapabilityEntity.builder()
                        .bootcampId(1L)
                        .capabilityId(2L)
                        .build();

        Bootcamp mappedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .capabilities(List.of())
                .build();

        when(bootcampEntityMapper.toEntity(bootcamp))
                .thenReturn(bootcampEntity);

        when(iBootcampRepository.save(bootcampEntity))
                .thenReturn(Mono.just(savedBootcampEntity));

        when(iBootcampCapabilityRepository.saveAll(anyList()))
                .thenReturn(
                        Flux.just(
                                capabilityEntity1,
                                capabilityEntity2
                        )
                );

        when(bootcampEntityMapper.toCapability(capabilityEntity1))
                .thenReturn(capability1);

        when(bootcampEntityMapper.toCapability(capabilityEntity2))
                .thenReturn(capability2);

        when(bootcampEntityMapper.toDomain(savedBootcampEntity))
                .thenReturn(mappedBootcamp);

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampPersistenceAdapter.save(bootcamp)
                )
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertEquals(
                            "Desarrollo Backend",
                            result.getName()
                    );
                    assertEquals(
                            "Bootcamp de desarrollo backend",
                            result.getDescription()
                    );
                    assertEquals(
                            List.of(capability1, capability2),
                            result.getCapabilities()
                    );
                })
                .verifyComplete();

        verify(bootcampEntityMapper).toEntity(bootcamp);
        verify(iBootcampRepository).save(bootcampEntity);
        verify(iBootcampCapabilityRepository).saveAll(anyList());
        verify(bootcampEntityMapper).toCapability(capabilityEntity1);
        verify(bootcampEntityMapper).toCapability(capabilityEntity2);
        verify(bootcampEntityMapper).toDomain(savedBootcampEntity);
    }

    @Test
    void shouldCheckIfBootcampExistsByName() {

        String name = "Desarrollo Backend";

        when(iBootcampRepository.existsByName(name))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        bootcampPersistenceAdapter.existsByName(name)
                )
                .expectNext(true)
                .verifyComplete();

        verify(iBootcampRepository).existsByName(name);
    }

    @Test
    void shouldReturnFalseWhenBootcampDoesNotExistByName() {

        String name = "Desarrollo Backend";

        when(iBootcampRepository.existsByName(name))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        bootcampPersistenceAdapter.existsByName(name)
                )
                .expectNext(false)
                .verifyComplete();

        verify(iBootcampRepository).existsByName(name);
    }

    @Test
    void shouldPropagateErrorWhenSavingBootcamp() {

        Bootcamp bootcamp = Bootcamp.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .capabilities(
                        List.of(
                                Capability.builder()
                                        .id(1L)
                                        .build()
                        )
                )
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .build();

        RuntimeException exception =
                new RuntimeException("Error guardando bootcamp");

        when(bootcampEntityMapper.toEntity(bootcamp))
                .thenReturn(bootcampEntity);

        when(iBootcampRepository.save(bootcampEntity))
                .thenReturn(Mono.error(exception));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampPersistenceAdapter.save(bootcamp)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException
                                && error.getMessage()
                                .equals("Error guardando bootcamp")
                )
                .verify();

        verify(bootcampEntityMapper).toEntity(bootcamp);
        verify(iBootcampRepository).save(bootcampEntity);
        verifyNoInteractions(iBootcampCapabilityRepository);
    }

    @Test
    void shouldPropagateErrorWhenSavingBootcampCapabilities() {

        Capability capability = Capability.builder()
                .id(1L)
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .capabilities(List.of(capability))
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .build();

        BootcampEntity savedBootcampEntity = BootcampEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp de desarrollo backend")
                .build();

        RuntimeException exception =
                new RuntimeException(
                        "Error guardando relaciones del bootcamp"
                );

        when(bootcampEntityMapper.toEntity(bootcamp))
                .thenReturn(bootcampEntity);

        when(iBootcampRepository.save(bootcampEntity))
                .thenReturn(Mono.just(savedBootcampEntity));

        when(iBootcampCapabilityRepository.saveAll(anyList()))
                .thenReturn(Flux.error(exception));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampPersistenceAdapter.save(bootcamp)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException
                                && error.getMessage()
                                .equals(
                                        "Error guardando relaciones del bootcamp"
                                )
                )
                .verify();

        verify(bootcampEntityMapper).toEntity(bootcamp);
        verify(iBootcampRepository).save(bootcampEntity);
        verify(iBootcampCapabilityRepository).saveAll(anyList());
    }

    @Test
    void shouldSaveBootcampWithoutCapabilities() {

        Bootcamp bootcamp = Bootcamp.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp sin capacidades")
                .capabilities(List.of())
                .build();

        BootcampEntity bootcampEntity = BootcampEntity.builder()
                .name("Desarrollo Backend")
                .description("Bootcamp sin capacidades")
                .build();

        BootcampEntity savedBootcampEntity = BootcampEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp sin capacidades")
                .build();

        Bootcamp mappedBootcamp = Bootcamp.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Bootcamp sin capacidades")
                .capabilities(List.of())
                .build();

        when(bootcampEntityMapper.toEntity(bootcamp))
                .thenReturn(bootcampEntity);

        when(iBootcampRepository.save(bootcampEntity))
                .thenReturn(Mono.just(savedBootcampEntity));

        when(iBootcampCapabilityRepository.saveAll(anyList()))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(savedBootcampEntity))
                .thenReturn(mappedBootcamp);

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        bootcampPersistenceAdapter.save(bootcamp)
                )
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertEquals(
                            "Desarrollo Backend",
                            result.getName()
                    );
                    assertTrue(result.getCapabilities().isEmpty());
                })
                .verifyComplete();

        verify(iBootcampRepository).save(bootcampEntity);
        verify(iBootcampCapabilityRepository).saveAll(anyList());
        verify(bootcampEntityMapper).toDomain(savedBootcampEntity);
    }

    @Test
    void shouldPropagateErrorWhenCheckingBootcampExistsByName() {

        String name = "Desarrollo Backend";

        RuntimeException exception =
                new RuntimeException(
                        "Error consultando bootcamp"
                );

        when(iBootcampRepository.existsByName(name))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        bootcampPersistenceAdapter.existsByName(name)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException
                                && error.getMessage()
                                .equals("Error consultando bootcamp")
                )
                .verify();

    }

    @Test
    void shouldFindAllBootcampsOrderByNameAsc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        BootcampCapabilityEntity capabilityEntity =
                BootcampCapabilityEntity.builder()
                        .bootcampId(1L)
                        .capabilityId(10L)
                        .build();

        when(iBootcampRepository.findAllOrderByNameAsc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.just(capabilityEntity));

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0,
                                10,
                                "name",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.content().getFirst().getId());
                    assertEquals("Bootcamp Backend",
                            result.content().getFirst().getName());

                    assertEquals(
                            List.of(
                                    Capability.builder()
                                            .id(10L)
                                            .build()
                            ),
                            result.content().getFirst().getCapabilities()
                    );

                    assertEquals(0, result.page());
                    assertEquals(10, result.size());
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                    assertTrue(result.first());
                    assertTrue(result.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllBootcampsOrderByNameDesc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(2L)
                .name("Bootcamp Java")
                .description("Java")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(2L)
                .name("Bootcamp Java")
                .description("Java")
                .build();

        when(iBootcampRepository.findAllOrderByNameDesc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(2L))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0,
                                10,
                                "name",
                                "desc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals("Bootcamp Java",
                            result.content().getFirst().getName());
                    assertTrue(
                            result.content().getFirst().getCapabilities().isEmpty()
                    );
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllBootcampsOrderByNumberCapabilitiesAsc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        when(iBootcampRepository.findAllOrderByCapabilityCountAsc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0,
                                10,
                                "numberCapabilities",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                })
                .verifyComplete();

    }

    @Test
    void shouldFindAllBootcampsOrderByNumberCapabilitiesDesc() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        when(iBootcampRepository.findAllOrderByCapabilityCountDesc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0,
                                10,
                                "numberCapabilities",
                                "desc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                })
                .verifyComplete();

    }

    @Test
    void shouldUseDefaultSortWhenSortByIsInvalid() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(1L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        when(iBootcampRepository.findAllOrderByNameAsc(10, 0L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(1L))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                0,
                                10,
                                "invalidSort",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.content().size());
                    assertEquals(1L, result.totalElements());
                    assertEquals(1, result.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnMiddlePageCorrectly() {

        BootcampEntity entity = BootcampEntity.builder()
                .id(11L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        Bootcamp bootcamp = Bootcamp.builder()
                .id(11L)
                .name("Bootcamp Backend")
                .description("Backend")
                .build();

        when(iBootcampRepository.findAllOrderByNameAsc(10, 10L))
                .thenReturn(Flux.just(entity));

        when(iBootcampCapabilityRepository.findAllByBootcampId(11L))
                .thenReturn(Flux.empty());

        when(bootcampEntityMapper.toDomain(entity))
                .thenReturn(bootcamp);

        when(iBootcampRepository.countAllBootcamps())
                .thenReturn(Mono.just(30L));

        StepVerifier.create(
                        bootcampPersistenceAdapter.findAll(
                                1,
                                10,
                                "name",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    assertEquals(1, result.page());
                    assertEquals(10, result.size());
                    assertEquals(30L, result.totalElements());
                    assertEquals(3, result.totalPages());

                    assertFalse(result.first());
                    assertFalse(result.last());
                })
                .verifyComplete();

        verify(iBootcampRepository)
                .findAllOrderByNameAsc(10, 10L);
    }

}

