package com.pragma.bootcamp_service.domain.validation.capability;


import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.validation.MaxCapabilitiesValidator;
import com.pragma.bootcamp_service.domain.validation.MinCapabilitiesValidator;
import com.pragma.bootcamp_service.domain.validation.ValidationUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;

public class DomainBootcampValidator {

    public void validateBootcampCommand(BootcampCommand command) {

        if (ValidationUtils.isBlank(command.name())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NAME_REQUIRED);
        }

        if (ValidationUtils.isBlank(command.description())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.DESCRIPTION_REQUIRED);
        }

        if (command.launchDate() == null ||
                command.launchDate().isBefore(LocalDate.now(ZoneId.of("America/Lima")))) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.LAUNCH_DATE_REQUIRED);
        }

        if (command.durationDay() == null || command.durationDay() <= 0) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.DURATION_DAY_REQUIRED);
        }

        if (command.capabilityIds() == null) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.CAPABILITIES_IDS_REQUIRED);
        }

        if (!MinCapabilitiesValidator.isValid(command.capabilityIds().size())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.CAPABILITIES_MIN_LENGTH);
        }

        if (!MaxCapabilitiesValidator.isValid(command.capabilityIds().size())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.CAPABILITIES_MAX_LENGTH);
        }

        if (new HashSet<>(command.capabilityIds()).size() != command.capabilityIds().size()) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.REPEATED_CAPABILITY);
        }

    }

}
