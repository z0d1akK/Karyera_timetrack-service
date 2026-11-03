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
public class DailySummaryResponseDto {

    private UUID employeeId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    @Schema(description = "Per-day totals")
    private List<DailySummaryDayDto> days;
}
