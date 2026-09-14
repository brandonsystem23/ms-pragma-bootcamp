package com.pragma.bootcamp_service.domain.api;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.PagedResult;
import com.pragma.bootcamp_service.domain.model.command.BootcampPageCommand;
import reactor.core.publisher.Mono;

public interface IBootcampRetrieveServicePort {

    Mono<PagedResult<Bootcamp>> getBootcamps(BootcampPageCommand command, String token);
}
