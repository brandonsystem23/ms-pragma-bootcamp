package com.pragma.bootcamp_service.infrastructure.input.rest;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
import com.pragma.bootcamp_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bootcamp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bootcamp", description = "Endpoint para gestion de bootcamps")
public class BootcampController {

    private final IBootcampHandler iBootcampHandler;

    @PostMapping("/create")
    @Operation(summary = "Crear bootcamp", description = "Crear una bootcamp. Requiere rol ADMINISTRADOR")
    public Mono<BootcampResponse> createBootcamp(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody BootcampRequest request) {

        log.info("Solicitud para crear una bootcamp");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iBootcampHandler.create(request, token);
    }


}
