package com.zodiakk.timetrackservice.integration.project.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.integration.exception.UpstreamUnavailableException;
import com.zodiakk.timetrackservice.integration.project.client.ProjectInternalClient;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDirectoryServiceTest {

    @Mock
    private ProjectInternalClient projectInternalClient;

    @InjectMocks
    private ProjectDirectoryService service;

    @Test
    void requireEligibleReturnsAssignment() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 19);
        UUID assignmentId = UUID.randomUUID();
        when(projectInternalClient.getWorkEligibility(projectId, employeeId, taskId, date))
                .thenReturn(new WorkEligibilityDto(
                        true, projectId, taskId, employeeId, date, assignmentId, true, true, true, false, null
                ));

        assertEquals(assignmentId, service.requireEligible(projectId, taskId, employeeId, date).projectAssignmentId());
    }

    @Test
    void ineligibleBecomesConflict() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 19);
        when(projectInternalClient.getWorkEligibility(projectId, employeeId, taskId, date))
                .thenReturn(new WorkEligibilityDto(
                        false, projectId, taskId, employeeId, date, null, false, true, false, false, "TASK_ARCHIVED"
                ));

        assertThrows(ConflictException.class, () -> service.requireEligible(projectId, taskId, employeeId, date));
    }

    @Test
    void unavailableProjectBecomes503() {
        when(projectInternalClient.getWorkEligibility(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new FeignException.ServiceUnavailable(
                        "down",
                        Request.create(Request.HttpMethod.GET, "/", Map.of(), null, StandardCharsets.UTF_8, null),
                        null,
                        Map.of()
                ));

        assertThrows(
                UpstreamUnavailableException.class,
                () -> service.requireEligible(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDate.now())
        );
    }
}
