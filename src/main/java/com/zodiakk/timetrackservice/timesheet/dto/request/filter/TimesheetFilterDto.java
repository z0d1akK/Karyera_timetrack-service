package com.zodiakk.timetrackservice.timesheet.dto.request.filter;

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
public class TimesheetFilterDto {

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Status")
    private TimesheetStatus status;

    @Schema(description = "Period start from (inclusive)")
    private LocalDate periodStartFrom;

    @Schema(description = "Period start to (inclusive)")
    private LocalDate periodStartTo;

    @Schema(description = "Period end from (inclusive)")
    private LocalDate periodEndFrom;

    @Schema(description = "Period end to (inclusive)")
    private LocalDate periodEndTo;
}
