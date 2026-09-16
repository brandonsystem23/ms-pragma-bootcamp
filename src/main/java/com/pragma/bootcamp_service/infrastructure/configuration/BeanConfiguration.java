package com.pragma.bootcamp_service.infrastructure.configuration;

import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampEnrollmentServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.service.CapabilityDetailService;
import com.pragma.bootcamp_service.domain.spi.IBootcampParticipantPersistencePort;
import com.pragma.bootcamp_service.domain.spi.IBootcampPersistencePort;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.domain.usecase.BootcampDeleteUseCase;
import com.pragma.bootcamp_service.domain.usecase.BootcampEnrollmentUseCase;
import com.pragma.bootcamp_service.domain.usecase.BootcampRegisterUseCase;
import com.pragma.bootcamp_service.domain.usecase.BootcampRetrieveUseCase;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampEnrollmentValidator;
import com.pragma.bootcamp_service.domain.validation.bootcamp.DomainBootcampValidator;
import com.pragma.bootcamp_service.domain.validation.bootcamp.BootcampValidator;
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
    public BootcampEnrollmentValidator bootcampEnrollmentValidator(
            IBootcampPersistencePort iBootcampPersistencePort,
            IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort
    ) {
        return new BootcampEnrollmentValidator(
                iBootcampPersistencePort,
                iBootcampParticipantPersistencePort
        );
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
    public IBootcampEnrollmentServicePort bootcampEnrollmentUseCase(
            IBootcampParticipantPersistencePort iBootcampParticipantPersistencePort,
            BootcampEnrollmentValidator bootcampEnrollmentValidator
    ) {
        return new BootcampEnrollmentUseCase(
                iBootcampParticipantPersistencePort,
                bootcampEnrollmentValidator
        );
    }

    @Bean
    public CapabilityDetailService capabilityDetailService(
            ICapabilityWebClientPort iCapabilityWebClientPort
    ) {
        return new CapabilityDetailService(iCapabilityWebClientPort);
    }
}
