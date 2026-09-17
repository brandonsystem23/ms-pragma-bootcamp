package com.pragma.bootcamp_service.application.mapper;

import com.pragma.bootcamp_service.application.dto.request.BootcampFilterDto;
import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampListItemResponse;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.CapabilityBasicResponse;
import com.pragma.bootcamp_service.application.dto.response.TechnologyBasicResponse;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.Technology;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampDtoMapper {

    BootcampCommand toCommand(BootcampRequest request);

    @Mapping(
            target = "numberCapabilities",
            expression = "java(bootcamp.getCapabilities() != null ? bootcamp.getCapabilities().size() : 0L)"
    )
    BootcampResponse toResponse(Bootcamp bootcamp);

    BootcampListItemResponse toListItemResponse(Bootcamp bootcamp);

    CapabilityBasicResponse toCapabilityBasicResponse(Capability capability);

    TechnologyBasicResponse toTechnologyBasicResponse(Technology technology);

    BootcampPageCommand toCommandPage(BootcampFilterDto dto);

}
