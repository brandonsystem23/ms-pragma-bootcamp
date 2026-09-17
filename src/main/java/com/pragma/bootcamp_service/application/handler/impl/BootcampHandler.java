package com.pragma.bootcamp_service.application.handler.impl;

import com.pragma.bootcamp_service.application.dto.request.BootcampEnrollmentRequest;
import com.pragma.bootcamp_service.application.dto.request.BootcampFilterDto;
import com.pragma.bootcamp_service.application.dto.request.BootcampRequest;
import com.pragma.bootcamp_service.application.dto.response.BootcampEnrollmentResponse;
import com.pragma.bootcamp_service.application.dto.response.BootcampResponse;
import com.pragma.bootcamp_service.application.dto.response.PagedBootcampResponse;
import com.pragma.bootcamp_service.application.handler.IBootcampHandler;
import com.pragma.bootcamp_service.application.mapper.BootcampDtoMapper;
import com.pragma.bootcamp_service.domain.api.IBootcampDeleteServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampEnrollmentServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRegisterServicePort;
import com.pragma.bootcamp_service.domain.api.IBootcampRetrieveServicePort;
import com.pragma.bootcamp_service.domain.model.command.BootcampEnrollmentCommand;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampHandler implements IBootcampHandler {

    private static final String MESSAGE = "Inscripción realizada exitosamente";

    private final IBootcampRegisterServicePort iBootcampRegisterServicePort;
    private final IBootcampRetrieveServicePort iBootcampRetrieveServicePort;
    private final IBootcampDeleteServicePort iBootcampDeleteServicePort;
    private final IBootcampEnrollmentServicePort iBootcampEnrollmentServicePort;
    private final BootcampDtoMapper bootcampDtoMapper;

    @Override
    public Mono<BootcampResponse> create(BootcampRequest request, String token) {
        return iBootcampRegisterServicePort
                .create(bootcampDtoMapper.toCommand(request), token)
                .map(bootcampDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedBootcampResponse> getBootcamps(BootcampFilterDto filter, String token) {

        BootcampPageCommand command = bootcampDtoMapper.toCommandPage(filter);

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

    @Override
    public Mono<BootcampEnrollmentResponse> enroll(BootcampEnrollmentRequest request, Long participantId) {
        BootcampEnrollmentCommand command = new BootcampEnrollmentCommand(request.bootcampId(), participantId);

        return iBootcampEnrollmentServicePort.enroll(command)
                .thenReturn(
                        BootcampEnrollmentResponse.builder()
                                .bootcampId(request.bootcampId())
                                .participantId(participantId)
                                .message(MESSAGE)
                                .build()
                );
    }
}
