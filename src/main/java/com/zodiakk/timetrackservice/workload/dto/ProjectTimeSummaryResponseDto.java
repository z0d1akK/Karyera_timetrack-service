package com.zodiakk.timetrackservice.workload.dto;

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
public class ProjectTimeSummaryResponseDto {

    private UUID projectId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private long approvedMinutes;

    private long pendingMinutes;

    private long draftMinutes;

    private List<ProjectEmployeeMinutesDto> employees;
}
