package com.zodiakk.timetrackservice.integration.employee.dto;

import java.util.UUID;

public record EmployeeAccessScopeDto(
        UUID employeeId,
        UUID actorEmployeeId,
        boolean self,
        boolean managerOfEmployee,
        boolean active
) {
}
