package com.zodiakk.timetrackservice.integration.project.dto;

import java.time.LocalDate;
import java.util.UUID;

public record WorkEligibilityDto(
        boolean eligible,
        UUID projectId,
        UUID taskId,
        UUID employeeId,
        LocalDate workDate,
        UUID projectAssignmentId,
        boolean projectActive,
        Boolean taskBelongsToProject,
        boolean assignmentActiveOnDate,
        Boolean taskArchived,
        String reasonCode
) {
}
