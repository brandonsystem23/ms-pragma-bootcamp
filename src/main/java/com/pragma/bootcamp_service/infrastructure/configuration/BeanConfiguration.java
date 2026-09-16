package com.pragma.bootcamp_service.infrastructure.configuration;

import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.domain.usecase.BootcampDeleteUseCase;
import com.pragma.bootcamp_service.domain.usecase.BootcampRegisterUseCase;
import com.pragma.bootcamp_service.domain.usecase.BootcampRetrieveUseCase;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import com.pragma.bootcamp_service.domain.validation.capability.BootcampValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

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
            IBootcampPersistencePort iBootcampPersistencePort,
            DomainBootcampValidator domainBootcampValidator,
            BootcampValidator bootcampValidator,
            TransactionalOperator transactionalOperator

    ) {
        return new BootcampRegisterUseCase(
                iBootcampPersistencePort,
                domainBootcampValidator,
                bootcampValidator,
                transactionalOperator
        );
    }

    @Bean
    public IBootcampRetrieveServicePort bootcampRetrieveUseCase(
            IBootcampPersistencePort iBootcampPersistencePort,
            DomainBootcampValidator domainBootcampValidator,
            CapabilityDetailService capabilityDetailService

    ) {
        return new BootcampRetrieveUseCase(
                iBootcampPersistencePort,
                domainBootcampValidator,
                capabilityDetailService
        );
    }

    @Bean
    public IBootcampDeleteServicePort bootcampDeleteUseCase(
            IBootcampPersistencePort iBootcampPersistencePort,
            ICapabilityWebClientPort iCapabilityWebClientPort,
            TransactionalOperator transactionalOperator
    ) {
        return new BootcampDeleteUseCase(
                iBootcampPersistencePort,
                iCapabilityWebClientPort,
                transactionalOperator
        );
    }

    @Bean
    public CapabilityDetailService capabilityDetailService(
            ICapabilityWebClientPort iCapabilityWebClientPort
    ) {
        return new CapabilityDetailService(iCapabilityWebClientPort);
    }
}
