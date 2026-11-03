package com.zodiakk.timetrackservice.schedule.dto.request.create;

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
public class WorkScheduleDayCreateRequestDto {

    @Schema(description = "Schedule id")
    @NotNull(message = ValidationMessages.SCHEDULE_ID_REQUIRED)
    private UUID scheduleId;

    @Schema(description = "Day of week")
    @NotNull(message = ValidationMessages.DAY_OF_WEEK_REQUIRED)
    private Short dayOfWeek;

    @Schema(description = "Planned minutes")
    @NotNull(message = ValidationMessages.PLANNED_MINUTES_REQUIRED)
    private Integer plannedMinutes;

    @Schema(description = "Start time")
    private LocalTime startTime;

    @Schema(description = "End time")
    private LocalTime endTime;
}
