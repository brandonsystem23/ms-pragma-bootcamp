package com.pragma.bootcamp_service.domain.validation;

public final class MinCapabilitiesValidator {

    private static final int MIN_LENGTH = 1;

    private MinCapabilitiesValidator() {
    }

    public static boolean isValid(int size) {
        return size >= MIN_LENGTH;
    }
}
