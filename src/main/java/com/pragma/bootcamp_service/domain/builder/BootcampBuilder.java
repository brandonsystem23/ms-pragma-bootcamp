package com.pragma.bootcamp_service.domain.builder;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;

public final class BootcampBuilder {

    private BootcampBuilder() {
    }

    public static Bootcamp buildBootcamp(BootcampCommand bootcampCommand) {
        return Bootcamp.builder()
                .name(bootcampCommand.name())
                .description(bootcampCommand.description())
                .launchDate(bootcampCommand.launchDate())
                .durationDay(bootcampCommand.durationDay())
                .status(true)
                .capabilities(bootcampCommand.capabilityIds().stream()
                        .map(BootcampBuilder::buildCapability)
                        .toList())
                .build();
    }

    public static Capability buildCapability(Long id) {
        return Capability.builder()
                .id(id)
                .build();
    }
}
