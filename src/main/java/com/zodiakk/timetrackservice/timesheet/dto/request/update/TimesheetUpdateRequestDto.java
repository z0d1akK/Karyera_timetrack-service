package com.zodiakk.timetrackservice.timesheet.dto.request.update;

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
public class TimesheetUpdateRequestDto {

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
}
