package com.pragma.bootcamp_service.application.handler.impl;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
import com.pragma.bootcamp_service.application.mapper.BootcampDtoMapper;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampHandler implements IBootcampHandler {

    private final IBootcampRegisterServicePort iBootcampRegisterServicePort;

    private final BootcampDtoMapper bootcampDtoMapper;

    @Override
    public Mono<BootcampResponse> create(BootcampRequest request, String token) {
        return iBootcampRegisterServicePort
                .create(bootcampDtoMapper.toCommand(request), token)
                .map(bootcampDtoMapper::toResponse);
    }
}