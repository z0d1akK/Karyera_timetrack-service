package com.zodiakk.timetrackservice.schedule.controller;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.action.EmployeeWorkScheduleAssignRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.EmployeeWorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.service.EmployeeWorkScheduleService;
import com.zodiakk.timetrackservice.schedule.service.WorkScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/time")
@RequiredArgsConstructor
@Tag(name = "Work schedules", description = "Work schedule templates and employee assignments")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    private final EmployeeWorkScheduleService employeeWorkScheduleService;

    @GetMapping("/work-schedules")
    @Operation(summary = "List work schedules")
    public ResponseEntity<Page<WorkScheduleResponseDto>> findAll(
            @Valid WorkScheduleFilterDto filter,
            @Valid PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(workScheduleService.findAll(filter, pageRequest));
    }

    @PostMapping("/work-schedules")
    @Operation(summary = "Create a work schedule")
    public ResponseEntity<WorkScheduleResponseDto> create(
            @Valid @RequestBody WorkScheduleCreateRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleService.create(request));
    }

    @GetMapping("/work-schedules/{id}")
    @Operation(summary = "Get a work schedule")
    public ResponseEntity<WorkScheduleResponseDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(workScheduleService.get(id));
    }

    @PatchMapping("/work-schedules/{id}")
    @Operation(summary = "Update a work schedule")
    public ResponseEntity<WorkScheduleResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkScheduleUpdateRequestDto request
    ) {
        return ResponseEntity.ok(workScheduleService.update(id, request));
    }

    @PostMapping("/employees/{id}/work-schedules")
    @Operation(summary = "Assign a work schedule to an employee")
    public ResponseEntity<EmployeeWorkScheduleResponseDto> assign(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeWorkScheduleAssignRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeWorkScheduleService.assign(id, request));
    }

    @GetMapping("/employees/{id}/work-schedules")
    @Operation(summary = "Work schedule assignment history")
    public ResponseEntity<List<EmployeeWorkScheduleResponseDto>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeWorkScheduleService.history(id));
    }
}
