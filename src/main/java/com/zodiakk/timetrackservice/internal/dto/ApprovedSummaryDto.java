package com.zodiakk.timetrackservice.internal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ApprovedSummaryDto(
        UUID employeeId,
        LocalDate periodStart,
        LocalDate periodEnd,
        long totalApprovedMinutes,
        List<ApprovedProjectMinutesDto> projects
) {
}
