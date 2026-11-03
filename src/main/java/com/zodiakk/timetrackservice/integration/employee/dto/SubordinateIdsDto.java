package com.zodiakk.timetrackservice.integration.employee.dto;

import java.util.List;
import java.util.UUID;

public record SubordinateIdsDto(
        UUID employeeId,
        List<UUID> subordinateIds
) {
}
