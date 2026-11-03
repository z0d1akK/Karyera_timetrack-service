package com.zodiakk.timetrackservice.workload.dto;

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
public class ProjectEmployeeMinutesDto {

    private UUID employeeId;

    private long approvedMinutes;

    private long pendingMinutes;

    private long draftMinutes;
}
