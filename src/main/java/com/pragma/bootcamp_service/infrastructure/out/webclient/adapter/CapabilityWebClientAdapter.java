package com.pragma.bootcamp_service.infrastructure.out.webclient.adapter;

import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.CapabilityDetailResponse;
import com.pragma.bootcamp_service.infrastructure.out.webclient.mapper.CapabilityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CapabilityWebClientAdapter implements ICapabilityWebClientPort {

    private final WebClient capabilityWebClient;
    private final String capabilityPath;
    private final String capabilityByIdsPath;
    private final String capabilityDeletePath;
    private final CapabilityMapper capabilityMapper;

    public CapabilityWebClientAdapter(
            @Qualifier("capabilityWebClient") WebClient capabilityWebClient,
            @Value("${clients.capability.path}") String capabilityPath,
            @Value("${clients.capability.by-ids-path}") String capabilityByIdsPath,
            @Value("${clients.capability.delete-path}") String capabilityDeletePath,
            CapabilityMapper capabilityMapper) {
        this.capabilityWebClient = capabilityWebClient;
        this.capabilityPath = capabilityPath;
        this.capabilityByIdsPath = capabilityByIdsPath;
        this.capabilityDeletePath = capabilityDeletePath;
        this.capabilityMapper = capabilityMapper;
    }

    @Override
    public Mono<List<Long>> existsByIds(List<Long> ids, String token) {

        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return bodyClientPath(capabilityWebClient, capabilityPath, mapHeaders, ids)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        this::handleClientError
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        this::handleServerError
                )
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
                .retry(2);
    }

    @Override
    public Mono<List<Capability>> findByIds(List<Long> ids, String token) {
        return getClientPath(capabilityWebClient, capabilityByIdsPath, token, ids)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        this::handleClientError
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        this::handleServerError
                )
                .bodyToFlux(CapabilityDetailResponse.class)
                .map(capabilityMapper::toModel)
                .collectList()
                .retry(2);
    }

    @Override
    public Mono<Void> deleteByIds(List<Long> ids, String token) {
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return bodyDeleteClientPath(capabilityWebClient, capabilityDeletePath, mapHeaders, ids)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        this::handleClientError
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        this::handleServerError
                )
                .bodyToMono(Void.class);
    }

    private static WebClient.ResponseSpec getClientPath(
            WebClient webClient,
            String path,
            String token,
            List<Long> ids) {

        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        log.info("Consultando capacidades por IDs: {}", ids);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("ids", idsParam)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();
    }

    private static WebClient.ResponseSpec bodyClientPath(
            WebClient webClient,
            String path,
            Map<String, String> mapHeaders,
            List<Long> ids) {

        log.info("Consultando capacidades con IDs: {}", ids);

        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> mapHeaders.forEach(httpHeaders::set))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(ids)
                .retrieve();
    }

    private static WebClient.ResponseSpec bodyDeleteClientPath(
            WebClient webClient,
            String path,
            Map<String, String> mapHeaders,
            List<Long> ids) {

        log.info("Eliminando capacidades con IDs: {}", ids);

        return webClient.method(HttpMethod.DELETE)
                .uri(path)
                .headers(httpHeaders -> mapHeaders.forEach(httpHeaders::set))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(ids)
                .retrieve();
    }

    private Mono<Throwable> handleClientError(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("Error al consultar las capacidades")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }

    private Mono<Throwable> handleServerError(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("Error interno del servicio de capacidades")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }
}
