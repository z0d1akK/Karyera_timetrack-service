package com.zodiakk.timetrackservice.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMinutesDto {

    private UUID projectId;

    @Schema(description = "Approved minutes on the project")
    private long approvedMinutes;
}
