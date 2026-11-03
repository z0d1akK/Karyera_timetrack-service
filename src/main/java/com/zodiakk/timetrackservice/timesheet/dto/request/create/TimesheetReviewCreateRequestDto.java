package com.zodiakk.timetrackservice.timesheet.dto.request.create;

import com.zodiakk.timetrackservice.common.enums.*;
import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetReviewCreateRequestDto {

    @Schema(description = "Timesheet id")
    @NotNull(message = ValidationMessages.TIMESHEET_ID_REQUIRED)
    private UUID timesheetId;

    @Schema(description = "Decision")
    @NotNull(message = ValidationMessages.DECISION_REQUIRED)
    private TimesheetReviewDecision decision;

    @Schema(description = "Comment")
    private String comment;
}
