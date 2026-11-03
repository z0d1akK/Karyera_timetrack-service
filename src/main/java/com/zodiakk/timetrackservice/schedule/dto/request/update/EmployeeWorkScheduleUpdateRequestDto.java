package com.zodiakk.timetrackservice.schedule.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkScheduleUpdateRequestDto {

    @Schema(description = "Valid from")
    private LocalDate validFrom;

    @Schema(description = "Valid to")
    private LocalDate validTo;
}
