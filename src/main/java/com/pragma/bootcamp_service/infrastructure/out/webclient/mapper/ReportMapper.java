package com.pragma.bootcamp_service.infrastructure.out.webclient.mapper;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.Technology;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryCapabilityRequest;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryCreateRequest;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryTechnologyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "bootcampId", source = "id")
    BootcampHistoryCreateRequest toRequest(Bootcamp bootcamp);

    BootcampHistoryCapabilityRequest toRequestCapability(Capability capability);

    BootcampHistoryTechnologyRequest toRequestTechnology(Technology technology);


}
