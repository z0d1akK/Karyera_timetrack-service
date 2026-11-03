package com.zodiakk.timetrackservice.integration.employee.dto;

import java.util.List;

public record EmployeeLookupResponse(List<EmployeeLookupItem> employees) {
}
