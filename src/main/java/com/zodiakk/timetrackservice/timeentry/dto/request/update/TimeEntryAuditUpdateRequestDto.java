package com.zodiakk.timetrackservice.timeentry.dto.request.update;

import com.zodiakk.timetrackservice.common.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryAuditUpdateRequestDto {

    @Schema(description = "Actor employee id")
    private UUID actorEmployeeId;

    @Schema(description = "Action")
    private TimeEntryAuditAction action;

    @Schema(description = "Changes")
    private String changes;
}
