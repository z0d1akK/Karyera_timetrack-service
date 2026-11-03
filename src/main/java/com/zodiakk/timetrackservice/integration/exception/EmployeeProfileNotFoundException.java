package com.zodiakk.timetrackservice.integration.exception;

import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EmployeeProfileNotFoundException extends ResourceNotFoundException {

    public EmployeeProfileNotFoundException(UUID userId) {
        super(ErrorMessages.EMPLOYEE_PROFILE_NOT_FOUND.formatted(userId));
    }

    public static EmployeeProfileNotFoundException forEmployee(UUID employeeId) {
        return new EmployeeProfileNotFoundException(ErrorMessages.EMPLOYEE_NOT_FOUND.formatted(employeeId), employeeId);
    }

    private EmployeeProfileNotFoundException(String message, UUID ignored) {
        super(message);
    }
}
