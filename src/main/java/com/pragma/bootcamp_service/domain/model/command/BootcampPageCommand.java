package com.pragma.bootcamp_service.domain.model.command;

public record BootcampPageCommand (
        int page,
        int size,
        String sortBy,
        String direction
) {
}
