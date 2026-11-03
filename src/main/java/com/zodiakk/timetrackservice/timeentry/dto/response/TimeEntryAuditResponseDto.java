package com.zodiakk.timetrackservice.timeentry.dto.response;

import com.zodiakk.timetrackservice.common.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryAuditResponseDto {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Time entry id")
    private UUID timeEntryId;

    @Schema(description = "Actor employee id")
    private UUID actorEmployeeId;

    @Schema(description = "Action")
    private TimeEntryAuditAction action;

    @Schema(description = "Changes")
    private String changes;
}
