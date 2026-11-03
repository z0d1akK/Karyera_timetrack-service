package com.zodiakk.timetrackservice.internal.controller;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.internal.dto.ApprovedSummaryDto;
import com.zodiakk.timetrackservice.internal.dto.ApprovedTimeEntryDto;
import com.zodiakk.timetrackservice.internal.service.InternalTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/internal/v1/time")
@RequiredArgsConstructor
@Tag(name = "Internal time", description = "Approved time for other services")
public class InternalTimeController {

    private final InternalTimeService internalTimeService;

    @GetMapping("/approved-entries")
    @Operation(summary = "Approved time entries for a period")
    public ResponseEntity<Page<ApprovedTimeEntryDto>> approvedEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Valid PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(internalTimeService.approvedEntries(from, to, pageRequest));
    }

    @GetMapping("/employees/{employeeId}/approved-summary")
    @Operation(summary = "Approved minutes of an employee")
    public ResponseEntity<ApprovedSummaryDto> approvedSummary(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(internalTimeService.approvedSummary(employeeId, from, to));
    }
}
