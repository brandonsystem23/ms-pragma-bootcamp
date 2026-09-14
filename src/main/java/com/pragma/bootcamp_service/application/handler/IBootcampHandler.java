package com.pragma.bootcamp_service.application.handler;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHandler {

    Mono<BootcampResponse> create(BootcampRequest request, String token);

    Mono<PagedBootcampResponse> getBootcamps(
            int page,
            int size,
            String sortBy,
            String direction,
            String token
    );

}
