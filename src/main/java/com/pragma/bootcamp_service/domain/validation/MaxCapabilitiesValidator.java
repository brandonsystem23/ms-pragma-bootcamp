package com.pragma.bootcamp_service.domain.validation;

public final class MaxCapabilitiesValidator {

    private static final int MAX_LENGTH = 4;

    private MaxCapabilitiesValidator() {
    }

    public static boolean isValid(int size) {
        return size <= MAX_LENGTH;
    }
}
