package com.zodiakk.timetrackservice.schedule.dto.request.create;

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
public class EmployeeWorkScheduleCreateRequestDto {

    @Schema(description = "Employee id")
    @NotNull(message = ValidationMessages.EMPLOYEE_ID_REQUIRED)
    private UUID employeeId;

    @Schema(description = "Schedule id")
    @NotNull(message = ValidationMessages.SCHEDULE_ID_REQUIRED)
    private UUID scheduleId;

    @Schema(description = "Valid from")
    @NotNull(message = ValidationMessages.VALID_FROM_REQUIRED)
    private LocalDate validFrom;

    @Schema(description = "Valid to")
    private LocalDate validTo;
}
