package com.pragma.bootcamp_service.domain.exception;

public final class DomainErrorMessages {

    private DomainErrorMessages() {
    }

    public static final String NAME_REQUIRED = "El campo name es obligatorio";
    public static final String DESCRIPTION_REQUIRED = "El campo description es obligatorio";
    public static final String LAUNCH_DATE_REQUIRED = "El campo launchDate es obligatorio y debe ser una fecha futura";
    public static final String DURATION_DAY_REQUIRED = "El campo durationDay es obligatorio y debe ser un valor positivo";
    public static final String DUPLICATE_NAME = "El nombre del bootcamp ya está registrado";
    public static final String CAPABILITIES_MAX_LENGTH = "El número maximo de capacidades es 4";
    public static final String CAPABILITIES_MIN_LENGTH = "El número minimo de capacidades es 1";
    public static final String REPEATED_CAPABILITY = "No puede exisitir capacidades repetidas";
    public static final String CAPABILITIES_IDS_REQUIRED = "La lista de capacidades es obligatoria";
    public static final String CAPABILITY_NOT_FOUND = "Alguna de las capacidades ingresadas no existe";

}
