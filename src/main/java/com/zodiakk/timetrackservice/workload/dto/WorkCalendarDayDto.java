package com.zodiakk.timetrackservice.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkCalendarDayDto {

    private LocalDate date;

    private int plannedMinutes;

    private UUID scheduleId;

    private String scheduleName;
}
