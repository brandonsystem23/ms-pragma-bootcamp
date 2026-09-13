package com.pragma.bootcamp_service.infrastructure.configuration;

import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.domain.usecase.BootcampRegisterUseCase;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import com.pragma.bootcamp_service.domain.validation.capability.BootcampValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {


    @Bean
    public DomainBootcampValidator domainBootcampValidator() {
        return new DomainBootcampValidator();
    }


    @Bean
    public BootcampValidator bootcampValidator(IBootcampPersistencePort iCapabilityPersistencePort,
                                                 ICapabilityWebClientPort iTechnologyWebClientPort) {
        return new BootcampValidator(iCapabilityPersistencePort, iTechnologyWebClientPort);
    }

    @Bean
    public IBootcampRegisterServicePort bootcampRegisterUseCase(
            IBootcampPersistencePort iCapabilityPersistencePort,
            DomainBootcampValidator domainCapabilityValidator,
            BootcampValidator capabilityValidator

    ) {
        return new BootcampRegisterUseCase(
                iCapabilityPersistencePort,
                domainCapabilityValidator,
                capabilityValidator
        );
    }


}
