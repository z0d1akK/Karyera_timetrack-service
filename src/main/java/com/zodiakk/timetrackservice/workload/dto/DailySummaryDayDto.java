package com.zodiakk.timetrackservice.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class DailySummaryDayDto {

    @Schema(description = "Date")
    private LocalDate date;

    @Schema(description = "Draft and rejected minutes")
    private long draftMinutes;

    @Schema(description = "Submitted minutes")
    private long submittedMinutes;

    @Schema(description = "Approved minutes")
    private long approvedMinutes;

    @Schema(description = "Rejected minutes")
    private long rejectedMinutes;

    @Schema(description = "Minutes excluding voided entries")
    private long totalMinutes;
}
