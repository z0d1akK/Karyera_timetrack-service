package com.zodiakk.timetrackservice.timeentry.controller;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.action.TimeEntryVoidRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryUpdateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryAuditResponseDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;
import com.zodiakk.timetrackservice.timeentry.service.TimeEntryService;
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
@Tag(name = "Time entries", description = "Registration of time spent on project tasks")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping("/entries")
    @Operation(summary = "Search time entries")
    public ResponseEntity<Page<TimeEntryResponseDto>> findAll(
            @Valid TimeEntryFilterDto filter,
            @Valid PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(timeEntryService.findAll(filter, pageRequest));
    }

    @PostMapping("/entries")
    @Operation(summary = "Create a time entry")
    public ResponseEntity<TimeEntryResponseDto> create(@Valid @RequestBody TimeEntryCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryService.create(request));
    }

    @GetMapping("/entries/{id}")
    @Operation(summary = "Get a time entry")
    public ResponseEntity<TimeEntryResponseDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(timeEntryService.get(id));
    }

    @PatchMapping("/entries/{id}")
    @Operation(summary = "Update a draft or rejected time entry")
    public ResponseEntity<TimeEntryResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TimeEntryUpdateRequestDto request
    ) {
        return ResponseEntity.ok(timeEntryService.update(id, request));
    }

    @PostMapping("/entries/{id}/void")
    @Operation(summary = "Void a draft or rejected time entry")
    public ResponseEntity<TimeEntryResponseDto> voidEntry(
            @PathVariable UUID id,
            @Valid @RequestBody TimeEntryVoidRequestDto request
    ) {
        return ResponseEntity.ok(timeEntryService.voidEntry(id, request));
    }

    @GetMapping("/entries/{id}/audit")
    @Operation(summary = "Time entry change history")
    public ResponseEntity<List<TimeEntryAuditResponseDto>> audit(@PathVariable UUID id) {
        return ResponseEntity.ok(timeEntryService.audit(id));
    }
}
