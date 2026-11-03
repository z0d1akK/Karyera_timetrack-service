package com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.dto.SortDirection;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

@UtilityClass
public class TimesheetPageableFactory {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.ofEntries(
            Map.entry("periodStart", "periodStart"),
            Map.entry("periodEnd", "periodEnd"),
            Map.entry("status", "status"),
            Map.entry("plannedMinutes", "plannedMinutes"),
            Map.entry("submittedAt", "submittedAt"),
            Map.entry("approvedAt", "approvedAt"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt"),
            Map.entry("version", "version")
    );

    public static Pageable create(PageRequestDto pageRequest) {
        String sortField = ALLOWED_SORT_FIELDS.getOrDefault(
                pageRequest.getSortBy(),
                "periodStart"
        );

        Sort.Direction direction = pageRequest.getSortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(direction, sortField));
    }
}
