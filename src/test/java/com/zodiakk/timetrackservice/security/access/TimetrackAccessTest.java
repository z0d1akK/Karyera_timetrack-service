package com.zodiakk.timetrackservice.security.access;

import com.zodiakk.timetrackservice.common.constants.SecurityConstants;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeAccessScopeDto;
import com.zodiakk.timetrackservice.integration.employee.service.EmployeeDirectoryService;
import com.zodiakk.timetrackservice.security.principal.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimetrackAccessTest {

    @Mock
    private EmployeeDirectoryService employeeDirectory;

    @InjectMocks
    private TimetrackAccess access;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hrCanViewAnyoneAndSeesAllEmployees() {
        authenticate(SecurityConstants.HR_AUTHORITY);
        access.requireCanView(UUID.randomUUID());
        access.requireCanReviewQueue();
        access.requireCanViewProjectSummary();
        assertNull(access.visibleEmployeeIds());
    }

    @Test
    void employeeSeesOnlySelf() {
        UUID userId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        authenticate(userId, SecurityConstants.EMPLOYEE_AUTHORITY);
        when(employeeDirectory.currentEmployeeId()).thenReturn(employeeId);

        assertThrows(AccessDeniedException.class, () -> access.requireCanReviewQueue());
        assertEquals(Set.of(employeeId), access.visibleEmployeeIds());
    }

    @Test
    void selfApprovalIsForbidden() {
        UUID employeeId = UUID.randomUUID();
        authenticate(SecurityConstants.ADMIN_AUTHORITY);
        when(employeeDirectory.currentEmployeeId()).thenReturn(employeeId);

        assertThrows(AccessDeniedException.class, () -> access.requireCanApprove(employeeId));
    }

    @Test
    void managerOfEmployeeCanApprove() {
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        authenticate(SecurityConstants.MANAGER_AUTHORITY);
        when(employeeDirectory.currentEmployeeId()).thenReturn(actor);
        when(employeeDirectory.getAccessScope(target, actor))
                .thenReturn(new EmployeeAccessScopeDto(target, actor, false, true, true));
        when(employeeDirectory.subordinateIds(actor)).thenReturn(List.of(target));

        access.requireCanApprove(target);
        assertTrue(access.visibleEmployeeIds().contains(target));
    }

    private void authenticate(String role) {
        authenticate(UUID.randomUUID(), role);
    }

    private void authenticate(UUID userId, String role) {
        CurrentUser principal = CurrentUser.builder()
                .userId(userId)
                .role(role)
                .email("user@company.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }
}
