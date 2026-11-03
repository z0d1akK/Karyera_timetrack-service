package com.zodiakk.timetrackservice.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDayResponseDto {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Schedule id")
    private UUID scheduleId;

    @Schema(description = "Day of week")
    private Short dayOfWeek;

    @Schema(description = "Planned minutes")
    private Integer plannedMinutes;

    @Schema(description = "Start time")
    private LocalTime startTime;

    @Schema(description = "End time")
    private LocalTime endTime;
}
