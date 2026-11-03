package com.zodiakk.timetrackservice.integration.employee.dto;

import java.util.UUID;

public record EmployeeLookupItem(
        UUID id,
        String fullName,
        boolean active,
        UUID positionId,
        UUID gradeId
) {
}
