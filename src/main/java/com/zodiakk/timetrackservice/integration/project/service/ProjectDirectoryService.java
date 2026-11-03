package com.zodiakk.timetrackservice.integration.project.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.integration.exception.UpstreamUnavailableException;
import com.zodiakk.timetrackservice.integration.project.client.ProjectInternalClient;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectDirectoryService {

    private final ProjectInternalClient projectInternalClient;

    public WorkEligibilityDto requireEligible(UUID projectId, UUID taskId, UUID employeeId, LocalDate workDate) {
        WorkEligibilityDto eligibility = getEligibility(projectId, taskId, employeeId, workDate);

        if (!eligibility.eligible()) {
            String reason = eligibility.reasonCode() == null ? "NOT_ELIGIBLE" : eligibility.reasonCode();
            throw new ConflictException("Учёт времени по задаче недоступен: " + reason);
        }

        return eligibility;
    }

    private WorkEligibilityDto getEligibility(
            UUID projectId,
            UUID taskId,
            UUID employeeId,
            LocalDate workDate
    ) {
        try {
            return projectInternalClient.getWorkEligibility(projectId, employeeId, taskId, workDate);
        } catch (FeignException.NotFound ex) {
            throw new ConflictException("Учёт времени по задаче недоступен: PROJECT_NOT_FOUND");
        } catch (FeignException ex) {
            log.warn(
                    "Project work eligibility failed: projectId={}, taskId={}, employeeId={}",
                    projectId,
                    taskId,
                    employeeId,
                    ex
            );
            throw new UpstreamUnavailableException(ErrorMessages.PROJECT_SERVICE_UNAVAILABLE, ex);
        }
    }
}
