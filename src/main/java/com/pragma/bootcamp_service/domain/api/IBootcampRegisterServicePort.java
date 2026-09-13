package com.pragma.bootcamp_service.domain.api;

import com.pragma.bootcamp_service.domain.model.Bootcamp;
import com.pragma.bootcamp_service.domain.model.command.BootcampCommand;
import reactor.core.publisher.Mono;

public interface IBootcampRegisterServicePort {

    Mono<Bootcamp> create(BootcampCommand bootcampCommand, String token);
}
