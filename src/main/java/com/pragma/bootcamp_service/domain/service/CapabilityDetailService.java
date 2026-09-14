package com.pragma.bootcamp_service.domain.service;

import com.pragma.bootcamp_service.domain.model.Capability;
import com.pragma.bootcamp_service.domain.spi.ICapabilityWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CapabilityDetailService {

    private final ICapabilityWebClientPort iCapabilityWebClientPort;

    public Mono<List<Capability>> enrich(List<Capability> capabilities, String token) {

        List<Long> ids = capabilities.stream()
                .map(Capability::getId)
                .distinct()
                .toList();

        return iCapabilityWebClientPort.findByIds(ids, token);
    }
}