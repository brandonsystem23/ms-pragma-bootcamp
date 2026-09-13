package com.pragma.bootcamp_service.domain.model.command;

import java.time.LocalDate;
import java.util.List;

public record BootcampCommand(
        String name,
        String description,
        LocalDate launchDate,
        Integer durationDay,
        List<Long> capabilityIds){
}
