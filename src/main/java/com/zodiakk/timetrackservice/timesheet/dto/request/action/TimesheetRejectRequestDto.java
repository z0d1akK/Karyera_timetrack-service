package com.zodiakk.timetrackservice.timesheet.dto.request.action;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetRejectRequestDto {

    @Schema(description = "Expected entity version for optimistic locking")
    @NotNull(message = ValidationMessages.EXPECTED_VERSION_REQUIRED)
    private Long expectedVersion;

    @Schema(description = "Reason for returning the timesheet for revision")
    @NotBlank(message = ValidationMessages.COMMENT_REQUIRED)
    @Size(max = 2000, message = ValidationMessages.COMMENT_SIZE)
    private String comment;
}
