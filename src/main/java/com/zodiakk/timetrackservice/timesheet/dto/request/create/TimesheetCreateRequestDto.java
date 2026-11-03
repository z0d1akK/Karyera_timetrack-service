package com.zodiakk.timetrackservice.timesheet.dto.request.create;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetCreateRequestDto {

    @Schema(description = "Period start")
    @NotNull(message = ValidationMessages.PERIOD_START_REQUIRED)
    private LocalDate periodStart;

    @Schema(description = "Period end")
    @NotNull(message = ValidationMessages.PERIOD_END_REQUIRED)
    private LocalDate periodEnd;
}
