package com.zodiakk.timetrackservice.timesheet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetSubmitResponseDto {

    @Schema(description = "Submitted timesheet")
    private TimesheetResponseDto timesheet;

    @Schema(description = "Warnings that do not block submission")
    private List<String> warnings;
}
