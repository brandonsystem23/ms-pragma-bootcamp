package com.pragma.bootcamp_service.infrastructure.out.webclient.adapter;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.dto.ParticipantBootcampHistory;
import com.pragma.bootcamp_service.domain.spi.IReportWebClientPort;
import com.pragma.bootcamp_service.infrastructure.exception.ExternalServiceException;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryCreateRequest;
import com.pragma.bootcamp_service.infrastructure.out.webclient.dto.BootcampHistoryParticipantRequest;
import com.pragma.bootcamp_service.infrastructure.out.webclient.mapper.ReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BootcampHistoryWebClientAdapter implements IReportWebClientPort {

    private final WebClient reportWebClient;
    private final String createPath;
    private final String updatePath;
    private final ReportMapper reportMapper;

    public BootcampHistoryWebClientAdapter(
            @Qualifier("reportWebClient") WebClient reportWebClient,
            @Value("${clients.report.bootcamp-history-create-path}") String createPath,
            @Value("${clients.report.bootcamp-history-update-path}") String updatePath,
            ReportMapper reportMapper
    ) {
        this.reportWebClient = reportWebClient;
        this.createPath = createPath;
        this.updatePath = updatePath;
        this.reportMapper = reportMapper;
    }

    @Override
    public Mono<Void> createBootcampHistory(Bootcamp bootcamp, String token) {
        BootcampHistoryCreateRequest request = reportMapper.toRequest(bootcamp);

        log.info("Registrando histórico del bootcamp {}", bootcamp.getId());

        return reportWebClient.post()
                .uri(createPath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> updateBootcampHistoryParticipant(ParticipantBootcampHistory participantBootcampHistory,
                                                       Long bootcampId, String token) {

        BootcampHistoryParticipantRequest body = BootcampHistoryParticipantRequest.builder()
                .fullName(participantBootcampHistory.fullName())
                .email(participantBootcampHistory.email())
                .build();

        log.info("Actualizando histórico del bootcamp {} con participante {}", bootcampId,
                participantBootcampHistory.fullName());

        return reportWebClient.method(HttpMethod.PATCH)
                .uri(updatePath + "/" + bootcampId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(Void.class);
    }

    private Mono<Throwable> handleClientError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("Error en servicio externo de reportes")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }

    private Mono<Throwable> handleServerError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("Error interno del servicio de reportes")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }
}
