package com.zodiakk.timetrackservice.schedule.dto.request.action;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkScheduleAssignRequestDto {

    @Schema(description = "Work schedule identifier")
    @NotNull(message = ValidationMessages.SCHEDULE_ID_REQUIRED)
    private UUID scheduleId;

    @Schema(description = "Date from which the schedule applies")
    @NotNull(message = ValidationMessages.VALID_FROM_REQUIRED)
    private LocalDate validFrom;

    @Schema(description = "Date until which the schedule applies")
    private LocalDate validTo;
}
