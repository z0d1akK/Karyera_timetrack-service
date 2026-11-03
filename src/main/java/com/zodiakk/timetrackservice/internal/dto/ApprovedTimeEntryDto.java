package com.zodiakk.timetrackservice.internal.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApprovedTimeEntryDto(
        UUID timeEntryId,
        UUID employeeId,
        UUID projectId,
        UUID taskId,
        UUID projectAssignmentId,
        LocalDate workDate,
        Integer durationMinutes,
        OffsetDateTime approvedAt
) {
}
