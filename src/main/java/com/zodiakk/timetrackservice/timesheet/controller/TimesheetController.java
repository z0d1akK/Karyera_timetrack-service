package com.zodiakk.timetrackservice.timesheet.controller;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetApproveRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetRejectRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetSubmitRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.create.TimesheetCreateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetFilterDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetReviewResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetSubmitResponseDto;
import com.zodiakk.timetrackservice.timesheet.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Timesheets", description = "Weekly timesheets and approval")
public class TimesheetController {

    private final TimesheetService timesheetService;

    @GetMapping("/timesheets")
    @Operation(summary = "Search timesheets")
    public ResponseEntity<Page<TimesheetResponseDto>> findAll(
            @Valid TimesheetFilterDto filter,
            @Valid PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(timesheetService.findAll(filter, pageRequest));
    }

    @PostMapping("/timesheets")
    @Operation(summary = "Create a weekly timesheet")
    public ResponseEntity<TimesheetResponseDto> create(@Valid @RequestBody TimesheetCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timesheetService.create(request));
    }

    @GetMapping("/timesheets/pending-approval")
    @Operation(summary = "Timesheets waiting for a decision")
    public ResponseEntity<List<TimesheetResponseDto>> pendingApproval() {
        return ResponseEntity.ok(timesheetService.pendingApproval());
    }

    @GetMapping("/timesheets/{id}")
    @Operation(summary = "Timesheet details")
    public ResponseEntity<TimesheetResponseDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(timesheetService.get(id));
    }

    @GetMapping("/employees/{id}/timesheets")
    @Operation(summary = "Timesheets of an employee")
    public ResponseEntity<List<TimesheetResponseDto>> findByEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(timesheetService.findByEmployee(id));
    }

    @PostMapping("/timesheets/{id}/submit")
    @Operation(summary = "Submit a timesheet for approval")
    public ResponseEntity<TimesheetSubmitResponseDto> submit(
            @PathVariable UUID id,
            @Valid @RequestBody TimesheetSubmitRequestDto request
    ) {
        return ResponseEntity.ok(timesheetService.submit(id, request));
    }

    @PostMapping("/timesheets/{id}/approve")
    @Operation(summary = "Approve a timesheet")
    public ResponseEntity<TimesheetResponseDto> approve(
            @PathVariable UUID id,
            @Valid @RequestBody TimesheetApproveRequestDto request
    ) {
        return ResponseEntity.ok(timesheetService.approve(id, request));
    }

    @PostMapping("/timesheets/{id}/reject")
    @Operation(summary = "Return a timesheet for revision")
    public ResponseEntity<TimesheetResponseDto> reject(
            @PathVariable UUID id,
            @Valid @RequestBody TimesheetRejectRequestDto request
    ) {
        return ResponseEntity.ok(timesheetService.reject(id, request));
    }

    @GetMapping("/timesheets/{id}/reviews")
    @Operation(summary = "Approval history")
    public ResponseEntity<List<TimesheetReviewResponseDto>> reviews(@PathVariable UUID id) {
        return ResponseEntity.ok(timesheetService.reviews(id));
    }
}
