package com.pragma.bootcamp_service.infrastructure.out.webclient.mapper;

import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.Technology;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.CapabilityDetailResponse;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.TechnologyDetailResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    Capability toModel(CapabilityDetailResponse capabilityDetailResponse);

    Technology toTechnologyModel(TechnologyDetailResponse technologyDetailResponse);
}
