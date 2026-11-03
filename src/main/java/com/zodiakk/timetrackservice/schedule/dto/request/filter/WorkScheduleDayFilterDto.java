package com.zodiakk.timetrackservice.schedule.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDayFilterDto {

    @Schema(description = "Schedule id")
    private UUID scheduleId;

    @Schema(description = "Day of week")
    private Short dayOfWeek;
}
