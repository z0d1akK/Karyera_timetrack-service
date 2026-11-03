package com.zodiakk.timetrackservice.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkScheduleResponseDto {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Updated at")
    private OffsetDateTime updatedAt;

    @Schema(description = "Employee id")
    private UUID employeeId;

    @Schema(description = "Schedule id")
    private UUID scheduleId;

    @Schema(description = "Valid from")
    private LocalDate validFrom;

    @Schema(description = "Valid to")
    private LocalDate validTo;

    @Schema(description = "Resolved work schedule name")
    private String scheduleName;
}
