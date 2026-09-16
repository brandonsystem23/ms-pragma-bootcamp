package com.pragma.bootcamp_service.infrastructure.out.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bootcamp_participant")
public class BootcampParticipantEntity {

    @Id
    private Long id;

    @Column("bootcamp_id")
    private Long bootcampId;

    @Column("participant_id")
    private Long participantId;

    private Boolean status;
}
