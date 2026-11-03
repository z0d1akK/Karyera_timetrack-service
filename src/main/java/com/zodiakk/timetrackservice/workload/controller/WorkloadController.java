package com.zodiakk.timetrackservice.workload.controller;

import com.zodiakk.timetrackservice.workload.dto.DailySummaryResponseDto;
import com.zodiakk.timetrackservice.workload.dto.EmployeeWorkloadResponseDto;
import com.zodiakk.timetrackservice.workload.dto.ProjectTimeSummaryResponseDto;
import com.zodiakk.timetrackservice.workload.dto.TeamWorkloadResponseDto;
import com.zodiakk.timetrackservice.workload.dto.WorkCalendarResponseDto;
import com.zodiakk.timetrackservice.workload.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/time")
@RequiredArgsConstructor
@Tag(name = "Workload", description = "Planned and logged workload")
public class WorkloadController {

    private final WorkloadService workloadService;

    @GetMapping("/employees/{id}/daily-summary")
    @Operation(summary = "Daily logged minutes")
    public ResponseEntity<DailySummaryResponseDto> dailySummary(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(workloadService.dailySummary(id, from, to));
    }

    @GetMapping("/employees/{id}/weekly-summary")
    @Operation(summary = "Weekly workload")
    public ResponseEntity<EmployeeWorkloadResponseDto> weeklySummary(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return ResponseEntity.ok(workloadService.weeklySummary(id, weekStart));
    }

    @GetMapping("/employees/{id}/workload")
    @Operation(summary = "Employee workload for a period")
    public ResponseEntity<EmployeeWorkloadResponseDto> workload(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        return ResponseEntity.ok(workloadService.workload(id, periodStart, periodEnd));
    }

    @GetMapping("/employees/{id}/work-calendar")
    @Operation(summary = "Planned minutes by day")
    public ResponseEntity<WorkCalendarResponseDto> workCalendar(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(workloadService.workCalendar(id, from, to));
    }

    @GetMapping("/projects/{id}/time-summary")
    @Operation(summary = "Logged time of a project")
    public ResponseEntity<ProjectTimeSummaryResponseDto> projectSummary(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        return ResponseEntity.ok(workloadService.projectSummary(id, periodStart, periodEnd));
    }

    @GetMapping("/teams/workload")
    @Operation(summary = "Workload of the visible team")
    public ResponseEntity<TeamWorkloadResponseDto> teamWorkload(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        return ResponseEntity.ok(workloadService.teamWorkload(periodStart, periodEnd));
    }
}
