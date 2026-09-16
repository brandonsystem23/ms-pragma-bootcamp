package com.pragma.bootcamp_service.application.handler.impl;

import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
import com.pragma.bootcamp_service.application.mapper.BootcampDtoMapper;
import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampHandler implements IBootcampHandler {

    private final IBootcampRegisterServicePort iBootcampRegisterServicePort;
    private final IBootcampRetrieveServicePort iBootcampRetrieveServicePort;
    private final IBootcampDeleteServicePort iBootcampDeleteServicePort;
    private final BootcampDtoMapper bootcampDtoMapper;

    @Override
    public Mono<BootcampResponse> create(BootcampRequest request, String token) {
        return iBootcampRegisterServicePort
                .create(bootcampDtoMapper.toCommand(request), token)
                .map(bootcampDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedBootcampResponse> getBootcamps(int page, int size, String sortBy, String direction, String token) {
        BootcampPageCommand command = new BootcampPageCommand(page, size, sortBy, direction);

        return iBootcampRetrieveServicePort
                .getBootcamps(command, token)
                .map(result -> PagedBootcampResponse.builder()
                        .content(
                                result.content()
                                        .stream()
                                        .map(bootcampDtoMapper::toListItemResponse)
                                        .toList()
                        )
                        .page(result.page())
                        .size(result.size())
                        .totalElements(result.totalElements())
                        .totalPages(result.totalPages())
                        .first(result.first())
                        .last(result.last())
                        .build()
                );
    }

    @Override
    public Mono<Void> deleteById(Long id, String token) {
        return iBootcampDeleteServicePort.deleteById(id, token);
    }
}
