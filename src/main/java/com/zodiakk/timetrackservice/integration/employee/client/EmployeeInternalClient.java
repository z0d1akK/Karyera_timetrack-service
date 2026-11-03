package com.zodiakk.timetrackservice.integration.employee.client;

import com.zodiakk.timetrackservice.integration.client.InternalFeignConfig;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeAccessScopeDto;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupRequest;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupResponse;
import com.zodiakk.timetrackservice.integration.employee.dto.SubordinateIdsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "employee-service",
        url = "${services.employee.url}",
        configuration = InternalFeignConfig.class
)
public interface EmployeeInternalClient {

    @GetMapping("/internal/v1/employees/by-user/{userId}")
    EmployeeLookupItem getByUserId(@PathVariable("userId") UUID userId);

    @PostMapping("/internal/v1/employees/lookup")
    EmployeeLookupResponse lookup(@RequestBody EmployeeLookupRequest request);

    @GetMapping("/internal/v1/employees/{employeeId}/access-scope")
    EmployeeAccessScopeDto getAccessScope(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(value = "actorEmployeeId", required = false) UUID actorEmployeeId
    );

    @GetMapping("/internal/v1/employees/{employeeId}/subordinate-ids")
    SubordinateIdsDto getSubordinateIds(@PathVariable("employeeId") UUID employeeId);
}
