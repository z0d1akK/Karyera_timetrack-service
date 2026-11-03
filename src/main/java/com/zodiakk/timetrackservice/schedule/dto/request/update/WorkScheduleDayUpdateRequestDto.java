package com.zodiakk.timetrackservice.schedule.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDayUpdateRequestDto {

    @Schema(description = "Day of week")
    private Short dayOfWeek;

    @Schema(description = "Planned minutes")
    private Integer plannedMinutes;

    @Schema(description = "Start time")
    private LocalTime startTime;

    @Schema(description = "End time")
    private LocalTime endTime;
}
