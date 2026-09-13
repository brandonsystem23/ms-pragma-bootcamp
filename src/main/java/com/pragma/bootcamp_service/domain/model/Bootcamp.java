package com.pragma.bootcamp_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bootcamp {

    Long id;

    String name;

    String description;

    LocalDate launchDate;

    Integer durationDay;

    List<Capability> capabilities;
}
