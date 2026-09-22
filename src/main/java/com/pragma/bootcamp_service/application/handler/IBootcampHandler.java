package com.pragma.bootcamp_service.application.handler;

import com.pragma.bootcamp_service.application.dto.request.BootcampEnrollmentRequest;
import com.pragma.bootcamp_service.application.dto.request.BootcampFilterDto;
import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampEnrollmentResponse;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHandler {

    Mono<BootcampResponse> create(BootcampRequest request, String token);

    Mono<PagedBootcampResponse> getBootcamps(
            BootcampFilterDto filter,
            String token
    );

    Mono<Void> deleteById(Long id, String token);

    Mono<BootcampEnrollmentResponse> enroll(
            BootcampEnrollmentRequest request,
            Long participantId,
            String fullName,
            String email,
            String token
    );
}
