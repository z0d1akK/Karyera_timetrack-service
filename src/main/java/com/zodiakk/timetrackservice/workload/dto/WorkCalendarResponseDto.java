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
public class WorkCalendarResponseDto {

    private UUID employeeId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private int plannedMinutes;

    private List<WorkCalendarDayDto> days;
}
