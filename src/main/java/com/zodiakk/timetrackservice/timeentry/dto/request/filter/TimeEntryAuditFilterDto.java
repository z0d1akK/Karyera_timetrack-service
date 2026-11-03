package com.zodiakk.timetrackservice.timeentry.dto.request.filter;

import com.zodiakk.timetrackservice.common.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryAuditFilterDto {

    @Schema(description = "Time entry id")
    private UUID timeEntryId;

    @Schema(description = "Actor employee id")
    private UUID actorEmployeeId;

    @Schema(description = "Action")
    private TimeEntryAuditAction action;
}
