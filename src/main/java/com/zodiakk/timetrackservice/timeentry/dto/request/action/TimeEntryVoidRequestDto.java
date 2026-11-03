package com.zodiakk.timetrackservice.timeentry.dto.request.action;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryVoidRequestDto {

    @Schema(description = "Reason for voiding the time entry")
    @NotBlank(message = ValidationMessages.REASON_REQUIRED)
    @Size(max = 1000, message = ValidationMessages.REASON_SIZE)
    private String reason;

    @Schema(description = "Expected entity version for optimistic locking")
    private Long expectedVersion;
}
