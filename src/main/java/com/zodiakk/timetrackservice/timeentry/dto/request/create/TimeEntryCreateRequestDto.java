package com.zodiakk.timetrackservice.timeentry.dto.request.create;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryCreateRequestDto {

    @Schema(description = "Project id")
    @NotNull(message = ValidationMessages.PROJECT_ID_REQUIRED)
    private UUID projectId;

    @Schema(description = "Task id")
    @NotNull(message = ValidationMessages.TASK_ID_REQUIRED)
    private UUID taskId;

    @Schema(description = "Project assignment id")
    private UUID projectAssignmentId;

    @Schema(description = "Work date")
    @NotNull(message = ValidationMessages.WORK_DATE_REQUIRED)
    private LocalDate workDate;

    @Schema(description = "Duration minutes")
    @NotNull(message = ValidationMessages.DURATION_MINUTES_REQUIRED)
    private Integer durationMinutes;

    @Schema(description = "Description")
    private String description;
}
