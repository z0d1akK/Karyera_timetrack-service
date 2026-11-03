package com.zodiakk.timetrackservice.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamWorkloadResponseDto {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private List<EmployeeWorkloadResponseDto> employees;
}
