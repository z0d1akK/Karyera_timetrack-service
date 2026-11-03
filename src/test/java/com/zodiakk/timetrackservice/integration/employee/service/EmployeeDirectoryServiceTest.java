package com.zodiakk.timetrackservice.integration.employee.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.integration.employee.client.EmployeeInternalClient;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupResponse;
import com.zodiakk.timetrackservice.integration.employee.dto.SubordinateIdsDto;
import com.zodiakk.timetrackservice.integration.exception.EmployeeProfileNotFoundException;
import com.zodiakk.timetrackservice.integration.exception.UpstreamUnavailableException;
import com.zodiakk.timetrackservice.security.principal.CurrentUser;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeDirectoryServiceTest {

    @Mock
    private EmployeeInternalClient employeeInternalClient;

    @InjectMocks
    private EmployeeDirectoryService service;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentEmployeeIsResolvedByUser() {
        UUID userId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        authenticate(userId);
        when(employeeInternalClient.getByUserId(userId))
                .thenReturn(new EmployeeLookupItem(employeeId, "Ivan Petrov", true, null, null));

        assertEquals(employeeId, service.currentEmployeeId());
    }

    @Test
    void missingUserBecomesNotFound() {
        UUID userId = UUID.randomUUID();
        when(employeeInternalClient.getByUserId(userId)).thenThrow(notFound());

        assertThrows(EmployeeProfileNotFoundException.class, () -> service.getByUserId(userId));
    }

    @Test
    void lookupFailureBecomesUnavailable() {
        UUID userId = UUID.randomUUID();
        when(employeeInternalClient.getByUserId(userId)).thenThrow(new FeignException.ServiceUnavailable(
                "down",
                Request.create(Request.HttpMethod.GET, "/", Map.of(), null, StandardCharsets.UTF_8, null),
                null,
                Map.of()
        ));

        assertThrows(UpstreamUnavailableException.class, () -> service.getByUserId(userId));
    }

    @Test
    void requireActiveRejectsInactiveEmployee() {
        UUID employeeId = UUID.randomUUID();
        when(employeeInternalClient.lookup(any())).thenReturn(new EmployeeLookupResponse(List.of(
                new EmployeeLookupItem(employeeId, "Ivan Petrov", false, null, null)
        )));

        assertThrows(ConflictException.class, () -> service.requireActive(employeeId));
    }

    @Test
    void subordinateLookupReturnsIds() {
        UUID managerId = UUID.randomUUID();
        UUID subordinateId = UUID.randomUUID();
        when(employeeInternalClient.getSubordinateIds(managerId))
                .thenReturn(new SubordinateIdsDto(managerId, List.of(subordinateId)));

        assertEquals(List.of(subordinateId), service.subordinateIds(managerId));
    }

    @Test
    void accessScopeAndMissingLookupAreMapped() {
        UUID employeeId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(employeeInternalClient.getAccessScope(employeeId, actorId))
                .thenReturn(new com.zodiakk.timetrackservice.integration.employee.dto.EmployeeAccessScopeDto(
                        employeeId, actorId, false, true, true
                ));
        assertEquals(true, service.getAccessScope(employeeId, actorId).managerOfEmployee());

        when(employeeInternalClient.getAccessScope(employeeId, actorId)).thenThrow(notFound());
        assertThrows(EmployeeProfileNotFoundException.class, () -> service.getAccessScope(employeeId, actorId));

        when(employeeInternalClient.lookup(any())).thenReturn(new EmployeeLookupResponse(List.of()));
        assertThrows(EmployeeProfileNotFoundException.class, () -> service.requireActive(employeeId));

        when(employeeInternalClient.getSubordinateIds(employeeId)).thenReturn(new SubordinateIdsDto(employeeId, null));
        assertEquals(List.of(), service.subordinateIds(employeeId));
    }

    private void authenticate(UUID userId) {
        CurrentUser principal = CurrentUser.builder()
                .userId(userId)
                .role("ROLE_EMPLOYEE")
                .email("user@company.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
        );
    }

    private FeignException.NotFound notFound() {
        return new FeignException.NotFound(
                "missing",
                Request.create(Request.HttpMethod.GET, "/", Map.of(), null, StandardCharsets.UTF_8, null),
                null,
                Map.of()
        );
    }
}
