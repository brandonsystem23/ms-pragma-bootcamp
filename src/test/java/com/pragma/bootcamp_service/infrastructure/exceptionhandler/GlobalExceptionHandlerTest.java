package com.pragma.bootcamp_service.infrastructure.exceptionhandler;

import com.pragma.bootcamp_service.domain.exception.DomainErrorCode;
import com.pragma.bootcamp_service.domain.exception.DomainException;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private ServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();

        serverWebExchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/bootcamp/create").build()
        );
    }

    @Test
    void shouldHandleValidationDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.VALIDATION_ERROR,
                "El campo name es obligatorio"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("El campo name es obligatorio", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleCapabilityNotFoundDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.CAPABILITY_NOT_FOUNT,
                "Una o más capacidades no existen"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Una o más capacidades no existen", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleDuplicateNameDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.DUPLICATE_NAME,
                "El nombre del bootcamp ya está registrado"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals(
                "El nombre del bootcamp ya está registrado",
                response.message()
        );
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleInvalidTokenDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.INVALID_TOKEN,
                "Token inválido o expirado"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertEquals("Unauthorized", response.error());
        assertEquals("Token inválido o expirado", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleAccessDeniedDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.ACCESS_DENIED,
                "No tienes permisos para acceder a este recurso"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status());
        assertEquals("Forbidden", response.error());
        assertEquals(
                "No tienes permisos para acceder a este recurso",
                response.message()
        );
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleExternalServiceErrorDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.EXTERNAL_SERVICE_ERROR,
                "Error en servicio externo"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.status());
        assertEquals("Bad Gateway", response.error());
        assertEquals("Error en servicio externo", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleInternalErrorDomainException() {

        DomainException exception = new DomainException(
                DomainErrorCode.INTERNAL_ERROR,
                "Ocurrió un error interno en el servidor"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                responseEntity.getStatusCode()
        );
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.status()
        );
        assertEquals("Internal Server Error", response.error());
        assertEquals(
                "Ocurrió un error interno en el servidor",
                response.message()
        );
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleExternalServiceException() {

        ExternalServiceException exception = new ExternalServiceException(
                HttpStatus.NOT_FOUND,
                "Capacidades no encontradas"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleExternalServiceException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals("Not Found", response.error());
        assertEquals("Capacidades no encontradas", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleIllegalArgumentException() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Parámetro inválido");

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleIllegalArgument(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Parámetro inválido", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleGenericException() {

        Exception exception = new RuntimeException("Error inesperado");

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleGeneric(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                responseEntity.getStatusCode()
        );
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.status()
        );
        assertEquals("Internal Server Error", response.error());
        assertEquals(
                "Ocurrió un error interno en el servidor",
                response.message()
        );
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleValidationErrors() {

        WebExchangeBindException exception =
                mock(WebExchangeBindException.class);

        FieldError nameError = new FieldError(
                "bootcampRequest",
                "name",
                "El campo name es obligatorio"
        );

        FieldError descriptionError = new FieldError(
                "bootcampRequest",
                "description",
                "El campo description es obligatorio"
        );

        FieldError launchDateError = new FieldError(
                "bootcampRequest",
                "launchDate",
                "La fecha de lanzamiento es obligatoria"
        );

        when(exception.getFieldErrors())
                .thenReturn(List.of(
                        nameError,
                        descriptionError,
                        launchDateError
                ));

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleValidationErrors(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Error de validación", response.message());
        assertEquals("/api/v1/bootcamp/create", response.path());
        assertNotNull(response.timestamp());

        assertEquals(
                List.of(
                        "name: El campo name es obligatorio",
                        "description: El campo description es obligatorio",
                        "launchDate: La fecha de lanzamiento es obligatoria"
                ),
                response.details()
        );
    }

    private ErrorResponse getBody(
            ResponseEntity<ErrorResponse> responseEntity
    ) {
        ErrorResponse body = responseEntity.getBody();

        assertNotNull(body);

        return body;
    }
}

