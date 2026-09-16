package com.pragma.bootcamp_service.infrastructure.out.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bootcamp")
public class BootcampEntity {

    @Id
    private Long id;

    private String name;

    private String description;

    @Column("launch_date")
    private LocalDate launchDate;

    @Column("duration_day")
    private Integer durationDay;

    private Boolean status;


}
