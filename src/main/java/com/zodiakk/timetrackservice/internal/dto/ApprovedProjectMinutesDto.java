package com.zodiakk.timetrackservice.internal.dto;

import java.util.UUID;

public record ApprovedProjectMinutesDto(
        UUID projectId,
        long approvedMinutes
) {
}
