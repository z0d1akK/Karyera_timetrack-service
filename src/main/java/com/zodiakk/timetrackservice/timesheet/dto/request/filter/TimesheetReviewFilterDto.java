package com.zodiakk.timetrackservice.timesheet.dto.request.filter;

import com.zodiakk.timetrackservice.common.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetReviewFilterDto {

    @Schema(description = "Timesheet id")
    private UUID timesheetId;

    @Schema(description = "Reviewer employee id")
    private UUID reviewerEmployeeId;

    @Schema(description = "Decision")
    private TimesheetReviewDecision decision;
}
