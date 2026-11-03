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
public class TimesheetReviewUpdateRequestDto {

    @Schema(description = "Reviewer employee id")
    private UUID reviewerEmployeeId;

    @Schema(description = "Decision")
    private TimesheetReviewDecision decision;

    @Schema(description = "Comment")
    private String comment;

    @Schema(description = "Reviewed at")
    private OffsetDateTime reviewedAt;
}
