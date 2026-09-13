package com.pragma.bootcamp_service.infrastructure.out.webclient.adapter;

import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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

@Slf4j
@Component
public class CapabilityWebClientAdapter implements ICapabilityWebClientPort {

    private final WebClient capabilityWebClient;
    private final String capabilityPath;

    public CapabilityWebClientAdapter(
            @Qualifier("capabilityWebClient") WebClient capabilityWebClient,
            @Value("${clients.capability.path}") String capabilityPath) {
        this.capabilityWebClient = capabilityWebClient;
        this.capabilityPath = capabilityPath;
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
