package com.pragma.bootcamp_service.domain.validation;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainErrorMessages;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import com.pragma.bootcamp_service.domain.validation.capability.DomainBootcampValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class DomainBootcampValidatorTest {

    private DomainBootcampValidator domainBootcampValidator;

    @BeforeEach
    void setUp() {
        domainBootcampValidator = new DomainBootcampValidator();
    }

    @Test
    void shouldPassWhenCommandIsValid() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        Assertions.assertDoesNotThrow(() ->
                domainBootcampValidator.validateBootcampCommand(command)
        );
    }

    @Test
    void shouldFailWhenNameIsNull() {

        BootcampCommand command = new BootcampCommand(
                null,
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.NAME_REQUIRED
        );
    }

    @Test
    void shouldFailWhenNameIsBlank() {

        BootcampCommand command = new BootcampCommand(
                "   ",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.NAME_REQUIRED
        );
    }

    @Test
    void shouldFailWhenDescriptionIsNull() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                null,
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.DESCRIPTION_REQUIRED
        );
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "   ",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.DESCRIPTION_REQUIRED
        );
    }

    @Test
    void shouldFailWhenLaunchDateIsNull() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                null,
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.LAUNCH_DATE_REQUIRED
        );
    }

    @Test
    void shouldFailWhenLaunchDateIsInThePast() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().minusDays(1),
                30,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.LAUNCH_DATE_REQUIRED
        );
    }

    @Test
    void shouldFailWhenDurationDayIsNull() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                null,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.DURATION_DAY_REQUIRED
        );
    }

    @Test
    void shouldFailWhenDurationDayIsZero() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                0,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.DURATION_DAY_REQUIRED
        );
    }

    @Test
    void shouldFailWhenDurationDayIsNegative() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                -1,
                List.of(1L, 2L, 3L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.DURATION_DAY_REQUIRED
        );
    }

    @Test
    void shouldFailWhenCapabilityIdsAreNull() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                null
        );

        assertValidationError(
                command,
                DomainErrorMessages.CAPABILITIES_IDS_REQUIRED
        );
    }

    @Test
    void shouldFailWhenCapabilityIdsAreBelowMinimum() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of()
        );

        assertValidationError(
                command,
                DomainErrorMessages.CAPABILITIES_MIN_LENGTH
        );
    }

    @Test
    void shouldFailWhenCapabilityIdsExceedMaximum() {

        List<Long> capabilityIds = List.of(
                1L, 2L, 3L, 4L, 5L,
                6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L,
                16L, 17L, 18L, 19L, 20L, 21L
        );

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                capabilityIds
        );

        assertValidationError(
                command,
                DomainErrorMessages.CAPABILITIES_MAX_LENGTH
        );
    }

    @Test
    void shouldFailWhenCapabilityIdsAreRepeated() {

        BootcampCommand command = new BootcampCommand(
                "Desarrollo Backend",
                "Bootcamp de desarrollo backend",
                LocalDate.now().plusDays(1),
                30,
                List.of(1L, 2L, 2L)
        );

        assertValidationError(
                command,
                DomainErrorMessages.REPEATED_CAPABILITY
        );
    }

    private void assertValidationError(
            BootcampCommand command,
            String expectedMessage
    ) {

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainBootcampValidator.validateBootcampCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                expectedMessage,
                exception.getMessage()
        );
    }
}

