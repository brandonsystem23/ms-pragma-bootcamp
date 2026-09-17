package com.pragma.bootcamp_service.domain.validation.bootcamp;


import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.FilterValues;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import com.pragma.bootcamp_service.domain.validation.MaxCapabilitiesValidator;
import com.pragma.bootcamp_service.domain.validation.MinCapabilitiesValidator;
import com.pragma.bootcamp_service.domain.validation.ValidationUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

public class DomainBootcampValidator {

    private static final int NUMBER_PAGE_ZERO = 0;
    private static final int SIZE_ITEMS_ZERO = 0;

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

    public void validatePagination(BootcampPageCommand command) {

        if (command.page() < NUMBER_PAGE_ZERO) {
            throw new DomainException(
                    DomainErrorCode.INVALID_PAGE,
                    DomainErrorMessages.INVALID_PAGE
            );
        }

        if (command.size() <= SIZE_ITEMS_ZERO) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SIZE,
                    DomainErrorMessages.INVALID_SIZE
            );
        }

        if (!List.of(FilterValues.NAME, FilterValues.NUMBER_CAPABILITIES).contains(command.sortBy().toLowerCase())) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SORT_BY,
                    DomainErrorMessages.INVALID_SORT_BY
            );
        }

        if (!List.of(FilterValues.ASCENDING, FilterValues.DESCENDING).contains(command.direction().toLowerCase())) {
            throw new DomainException(
                    DomainErrorCode.INVALID_DIRECTION,
                    DomainErrorMessages.INVALID_DIRECTION
            );
        }
    }

}
