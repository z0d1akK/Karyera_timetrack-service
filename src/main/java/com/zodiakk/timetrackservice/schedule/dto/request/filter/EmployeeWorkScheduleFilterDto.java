package com.zodiakk.timetrackservice.schedule.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkScheduleFilterDto {

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Schedule id")
    private UUID scheduleId;

    @Schema(description = "Valid from date lower bound")
    private LocalDate validFromFrom;

    @Schema(description = "Valid from date upper bound")
    private LocalDate validFromTo;
}
