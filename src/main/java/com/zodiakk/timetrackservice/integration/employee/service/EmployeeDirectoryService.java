package com.zodiakk.timetrackservice.integration.employee.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.integration.employee.client.EmployeeInternalClient;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeAccessScopeDto;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupRequest;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupResponse;
import com.zodiakk.timetrackservice.integration.employee.dto.SubordinateIdsDto;
import com.zodiakk.timetrackservice.integration.exception.EmployeeProfileNotFoundException;
import com.zodiakk.timetrackservice.integration.exception.UpstreamUnavailableException;
import com.zodiakk.timetrackservice.security.util.SecurityUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeDirectoryService {

    private final EmployeeInternalClient employeeInternalClient;

    public EmployeeLookupItem getCurrentEmployee() {
        return getByUserId(SecurityUtils.getCurrentUserId());
    }

    public UUID currentEmployeeId() {
        return getCurrentEmployee().id();
    }

    public EmployeeLookupItem getByUserId(UUID userId) {
        try {
            return employeeInternalClient.getByUserId(userId);
        } catch (FeignException.NotFound ex) {
            throw new EmployeeProfileNotFoundException(userId);
        } catch (FeignException ex) {
            log.warn("Employee lookup by user failed: userId={}", userId, ex);
            throw unavailable(ex);
        }
    }

    public EmployeeLookupItem requireActive(UUID employeeId) {
        EmployeeLookupItem employee = require(employeeId);

        if (!employee.active()) {
            throw new ConflictException("Сотрудник неактивен");
        }

        return employee;
    }

    public EmployeeAccessScopeDto getAccessScope(UUID employeeId, UUID actorEmployeeId) {
        try {
            return employeeInternalClient.getAccessScope(employeeId, actorEmployeeId);
        } catch (FeignException.NotFound ex) {
            throw EmployeeProfileNotFoundException.forEmployee(employeeId);
        } catch (FeignException ex) {
            log.warn("Employee access scope failed: employeeId={}", employeeId, ex);
            throw unavailable(ex);
        }
    }

    public List<UUID> subordinateIds(UUID managerEmployeeId) {
        try {
            SubordinateIdsDto response = employeeInternalClient.getSubordinateIds(managerEmployeeId);

            if (response == null || response.subordinateIds() == null) {
                return List.of();
            }

            return List.copyOf(response.subordinateIds());
        } catch (FeignException.NotFound ex) {
            throw EmployeeProfileNotFoundException.forEmployee(managerEmployeeId);
        } catch (FeignException ex) {
            log.warn("Employee subordinate lookup failed: employeeId={}", managerEmployeeId, ex);
            throw unavailable(ex);
        }
    }

    private EmployeeLookupItem require(UUID employeeId) {
        try {
            EmployeeLookupResponse response = employeeInternalClient.lookup(
                    new EmployeeLookupRequest(List.of(employeeId))
            );

            if (response == null || response.employees() == null) {
                throw EmployeeProfileNotFoundException.forEmployee(employeeId);
            }

            return response.employees().stream()
                    .filter(item -> employeeId.equals(item.id()))
                    .findFirst()
                    .orElseThrow(() -> EmployeeProfileNotFoundException.forEmployee(employeeId));
        } catch (FeignException ex) {
            log.warn("Employee lookup failed: employeeId={}", employeeId, ex);
            throw unavailable(ex);
        }
    }

    private UpstreamUnavailableException unavailable(FeignException ex) {
        return new UpstreamUnavailableException(ErrorMessages.EMPLOYEE_SERVICE_UNAVAILABLE, ex);
    }
}
