package com.zodiakk.timetrackservice.timeentry.dto.request.create;

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
public class TimeEntryAuditCreateRequestDto {

    @Schema(description = "Time entry id")
    @NotNull(message = ValidationMessages.TIME_ENTRY_ID_REQUIRED)
    private UUID timeEntryId;

    @Schema(description = "Action")
    @NotNull(message = ValidationMessages.ACTION_REQUIRED)
    private TimeEntryAuditAction action;

    @Schema(description = "Changes")
    private String changes;
}
