package com.pragma.bootcamp_service.domain.usecase;

import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.builder.BootcampBuilder;
import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import com.pragma.bootcamp_service.domain.validation.capability.BootcampValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampRegisterUseCase implements IBootcampRegisterServicePort {

    private final IBootcampPersistencePort iBootcampPersistencePort;
    private final DomainBootcampValidator domainBootcampValidator;
    private final BootcampValidator bootcampValidator;


    @Override
    public Mono<Bootcamp> create(BootcampCommand bootcampCommand, String token) {
        return Mono.defer(() -> {
            domainBootcampValidator.validateBootcampCommand(bootcampCommand);

            return bootcampValidator.validateBootcamp(bootcampCommand.name(),
                            bootcampCommand.capabilityIds(), token)
                    .then(Mono.defer(() -> {
                        Bootcamp bootcamp = BootcampBuilder.buildBootcamp(bootcampCommand);
                        return iBootcampPersistencePort.save(bootcamp);
                    }));
        });
    }
}
