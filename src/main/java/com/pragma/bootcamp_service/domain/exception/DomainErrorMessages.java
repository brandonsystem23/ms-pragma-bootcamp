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
    public static final String INVALID_PAGE = "El parámetro page debe ser mayor o igual a 0";
    public static final String INVALID_SIZE = "El parámetro size debe ser mayor a 0";
    public static final String INVALID_SORT_BY = "El parámetro sortBy solo permite: name, numberCapabilities";
    public static final String INVALID_DIRECTION = "El parámetro direction solo permite: asc, desc";
    public static final String BOOTCAMP_ID_REQUIRED = "El id del bootcamp es obligatorio";
    public static final String PARTICIPANT_ID_REQUIRED = "El id del participante es obligatorio";
    public static final String RESOURCES_USED_BY_OTHER_BOOTCAMPS =
            "No se puede eliminar el bootcamp porque alguna capacidad o tecnología asociada está siendo usada en otro bootcamp";
    public static final String BOOTCAMP_SAVE_ROLLBACK_ERROR =
            "Ocurrió un error durante el registro transaccional de bootcamp. Se realizó rollback de la operación";
    public static final String BOOTCAMP_DELETE_ROLLBACK_ERROR =
            "Ocurrió un error durante la eliminación transaccional de bootcamp. Se realizó rollback de la operación";
    public static final String ROLLBACK_ERROR =
            "Ocurrió un error durante el proceso rollback de la operacion delete bootcamp";
    public static final String BOOTCAMP_NOT_FOUND = "El bootcamp no existe o no se encuentra activo";
    public static final String PARTICIPANT_ALREADY_ENROLLED = "El participante ya se encuentra inscrito en este bootcamp";
    public static final String MAX_ACTIVE_BOOTCAMPS_REACHED = "El participante no puede estar inscrito en más de 5 bootcamps al tiempo";
    public static final String BOOTCAMP_SCHEDULE_CONFLICT = "El participante ya tiene un bootcamp inscrito cuyo rango de fechas se cruza con el bootcamp solicitado";
    public static final String BOOTCAMP_HISTORY_SAVE_ERROR = "El bootcamp se registro correctamente pero no se pudo registrar el bootcamp history";
    public static final String BOOTCAMP_HISTORY_UPDATE_ERROR = "El bootcamp se actualizo correctamente pero no se pudo actualizar el bootcamp history";
}
