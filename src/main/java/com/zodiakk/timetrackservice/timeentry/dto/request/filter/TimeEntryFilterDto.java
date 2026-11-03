package com.zodiakk.timetrackservice.timeentry.dto.request.filter;

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
public class TimeEntryFilterDto {

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Project id")
    private UUID projectId;

    @Schema(description = "Task id")
    private UUID taskId;

    @Schema(description = "Timesheet id")
    private UUID timesheetId;

    @Schema(description = "Status")
    private TimeEntryStatus status;

    @Schema(description = "Work date from (inclusive)")
    private LocalDate workDateFrom;

    @Schema(description = "Work date to (inclusive)")
    private LocalDate workDateTo;
}
