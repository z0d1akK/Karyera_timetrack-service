package com.zodiakk.timetrackservice.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkloadResponseDto {

    private UUID employeeId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private int plannedMinutes;

    @Schema(description = "Approved minutes")
    private long approvedMinutes;

    @Schema(description = "Submitted minutes waiting for approval")
    private long pendingMinutes;

    @Schema(description = "Draft and rejected minutes")
    private long draftMinutes;

    @Schema(description = "Approved minutes grouped by project")
    private List<ProjectMinutesDto> projects;
}
