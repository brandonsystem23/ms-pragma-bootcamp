package com.pragma.bootcamp_service.infrastructure.out.mysql.mapper;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampCapabilityEntity;
import com.pragma.bootcamp_service.infrastructure.out.mysql.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampEntityMapper {

    @Mapping(target = "capabilities", ignore = true)
    Bootcamp toDomain(BootcampEntity bootcampEntity);

    BootcampEntity toEntity(Bootcamp bootcamp);

    @Mapping(target = "id", source = "bootcampCapabilityEntity.capabilityId")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "technologies", ignore = true)
    Capability toCapability(BootcampCapabilityEntity bootcampCapabilityEntity);

}
