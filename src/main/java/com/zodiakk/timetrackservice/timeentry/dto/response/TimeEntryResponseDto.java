package com.zodiakk.timetrackservice.timeentry.dto.response;

import com.zodiakk.timetrackservice.common.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryResponseDto {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Updated at")
    private OffsetDateTime updatedAt;

    @Schema(description = "Version")
    private Long version;

    @Schema(description = "Created by")
    private UUID createdBy;

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Project id")
    private UUID projectId;

    @Schema(description = "Task id")
    private UUID taskId;

    @Schema(description = "Project assignment id")
    private UUID projectAssignmentId;

    @Schema(description = "Work date")
    private LocalDate workDate;

    @Schema(description = "Duration minutes")
    private Integer durationMinutes;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Status")
    private TimeEntryStatus status;

    @Schema(description = "Timesheet id")
    private UUID timesheetId;

    @Schema(description = "Resolved project name")
    private String projectName;

    @Schema(description = "Resolved task title")
    private String taskTitle;
}
