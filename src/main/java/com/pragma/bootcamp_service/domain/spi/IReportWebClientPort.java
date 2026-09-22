package com.pragma.bootcamp_service.domain.spi;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.dto.ParticipantBootcampHistory;
import reactor.core.publisher.Mono;

public interface IReportWebClientPort {

    Mono<Void> createBootcampHistory(Bootcamp bootcamp, String token);

    Mono<Void> updateBootcampHistoryParticipant(ParticipantBootcampHistory participantBootcampHistory, Long bootcampId,
                                                String token);
}
