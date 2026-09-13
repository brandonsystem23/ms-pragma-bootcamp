package com.pragma.bootcamp_service.application.handler;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHandler {

    Mono<BootcampResponse> create(BootcampRequest request, String token);

}
