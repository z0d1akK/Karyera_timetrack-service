package com.zodiakk.timetrackservice.integration.project.client;

import com.zodiakk.timetrackservice.integration.client.InternalFeignConfig;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(
        name = "project-service",
        url = "${services.project.url}",
        configuration = InternalFeignConfig.class
)
public interface ProjectInternalClient {

    @GetMapping("/internal/v1/projects/{projectId}/work-eligibility")
    WorkEligibilityDto getWorkEligibility(
            @PathVariable("projectId") UUID projectId,
            @RequestParam("employeeId") UUID employeeId,
            @RequestParam("taskId") UUID taskId,
            @RequestParam("workDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    );
}
