package com.zodiakk.timetrackservice.timesheet.dto.response;

import com.zodiakk.timetrackservice.common.enums.*;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetResponseDto {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Updated at")
    private OffsetDateTime updatedAt;

    @Schema(description = "Version")
    private Long version;

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Period start")
    private LocalDate periodStart;

    @Schema(description = "Period end")
    private LocalDate periodEnd;

    @Schema(description = "Status")
    private TimesheetStatus status;

    @Schema(description = "Planned minutes")
    private Integer plannedMinutes;

    @Schema(description = "Submitted at")
    private OffsetDateTime submittedAt;

    @Schema(description = "Approved at")
    private OffsetDateTime approvedAt;

    @Schema(description = "Approved by")
    private UUID approvedBy;

    @Schema(description = "Time entries included in the timesheet")
    private List<TimeEntryResponseDto> entries;

    @Schema(description = "Total recorded minutes")
    private Long totalMinutes;
}
