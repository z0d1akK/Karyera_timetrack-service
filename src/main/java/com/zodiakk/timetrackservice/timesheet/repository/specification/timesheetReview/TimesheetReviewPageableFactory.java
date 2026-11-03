package com.zodiakk.timetrackservice.timesheet.repository.specification.timesheetReview;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.dto.SortDirection;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

@UtilityClass
public class TimesheetReviewPageableFactory {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.ofEntries(
            Map.entry("decision", "decision"),
            Map.entry("reviewedAt", "reviewedAt")
    );

    public static Pageable create(PageRequestDto pageRequest) {
        String sortField = ALLOWED_SORT_FIELDS.getOrDefault(
                pageRequest.getSortBy(),
                "reviewedAt"
        );

        Sort.Direction direction = pageRequest.getSortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(direction, sortField));
    }
}
